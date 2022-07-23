package com.skungee.velocity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bstats.charts.SimplePie;
import org.bstats.velocity.Metrics;
import org.reflections.Reflections;
import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.sitrica.japson.server.Connections.JapsonConnection;
import com.sitrica.japson.server.JapsonServer;
import com.sitrica.japson.server.Listener;
import com.sitrica.japson.shared.Executor;
import com.sitrica.japson.shared.Handler;
import com.skungee.proxy.ProxyPlatform;
import com.skungee.proxy.ProxySkungee;
import com.skungee.proxy.SkungeeAPI;
import com.skungee.proxy.managers.EventManager;
import com.skungee.proxy.managers.ServerDataManager;
import com.skungee.proxy.variables.VariableManager;
import com.skungee.shared.Packets;
import com.skungee.shared.objects.ServerData;
import com.skungee.shared.objects.SkungeePlayer;
import com.skungee.shared.objects.SkungeeServer;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.ServerInfo;

/**
 * Velocity
 */
@Plugin(id = "skungee", name = "Skungee", version = "2.0.0-ALPHA-20",
		description = "The ultimate Skript addon for Bungeecord.", authors = {"Skungee"})
public class VelocitySkungee implements ProxyPlatform {

	private final VelocityConfiguration configuration;
	private final ServerDataManager serverDataManager;
	private final File dataFolder, SCRIPTS_FOLDER;
	private final Metrics.Factory metricsFactory;
	private VariableManager variableManager;
	private EventManager eventManager;
	private final ProxyServer proxy;
	private final SkungeeAPI API;
	private final Logger logger;
	private JapsonServer japson;

	@Inject
	public VelocitySkungee(ProxyServer proxy, Logger logger, @DataDirectory Path path, Metrics.Factory metricsFactory) {
		this.proxy = proxy;
		this.logger = logger;
		this.metricsFactory = metricsFactory;
		serverDataManager = new ServerDataManager(this);
		eventManager = new EventManager(this);
		dataFolder = path.toFile();
		SCRIPTS_FOLDER = new File(dataFolder, File.separator + "scripts");
		if (!SCRIPTS_FOLDER.exists())
			SCRIPTS_FOLDER.mkdir();
		File file = new File(dataFolder, "config.toml");
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		if (!file.exists()) {
			try (InputStream input = getClass().getResourceAsStream("/" + file.getName())) {
				if (input != null) {
					Files.copy(input, file.toPath());
				} else {
					file.createNewFile();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		configuration = new VelocityConfiguration(new Toml().read(file), 1);
		try {
			ProxySkungee.setPlatform(this);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		API = new SkungeeAPI(this);
		logger.info("Skungee has been enabled!");
	}

	private final VelocitySkungee getSelf() {
		return this;
	}

	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
		variableManager = new VariableManager(this);
		try {
			japson = new JapsonServer(configuration.getBindAddress());
			japson.registerHandlers(new Reflections("com.skungee.proxy.handlers", "com.skungee.velocity.handlers")
					.getSubTypesOf(Handler.class).stream()
					.filter(clazz -> clazz != Executor.class)
					.map(clazz -> {
						try {
							return clazz.getConstructor(ProxyServer.class).newInstance(proxy);
						} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
							try {
								return clazz.getConstructor().newInstance();
							} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e1) {
								e.printStackTrace();
								return null;
							}
						}
					})
					.filter(handler -> handler != null)
					.toArray(Handler[]::new));
			if (configuration.isDebug()) {
				japson.enableDebug();
				if (configuration.getIgnoredDebugPackets().length != 0)
					japson.addIgnoreDebugPackets(configuration.getIgnoredDebugPackets());
			}
			japson.setPacketBufferSize(configuration.getBufferSize());
			List<InetAddress> address = Lists.newArrayList(InetAddress.getLocalHost(), InetAddress.getByName("127.0.0.1"));
			address.addAll(proxy.getAllServers().stream()
					.map(server -> server.getServerInfo().getAddress().getAddress())
					.collect(Collectors.toList()));
			address.addAll(configuration.getWhitelistedAddresses());
			japson.setAllowedAddresses(address.stream().toArray(InetAddress[]::new));
			japson.registerListener(new Listener() {
				@Override
				public void onAcquiredCommunication(JapsonConnection connection) {
					getSelf().debugMessage("Connection acquired " + connection.getAddress().toString());
				}

				@Override
				public void onDisconnect(JapsonConnection connection) {
					getSelf().debugMessage("Connection disconnected " + connection.getAddress().toString());
				}

				@Override
				public void onForget(JapsonConnection connection) {
					getSelf().debugMessage("Connection forgotten " + connection.getAddress().toString());
				}

				@Override
				public void onReacquiredCommunication(JapsonConnection connection) {
					getSelf().debugMessage("Connection reestablished " + connection.getAddress().toString());
				}

				@Override
				public void onShutdown() {}

				@Override
				public void onUnresponsive(JapsonConnection connection) {
					getSelf().debugMessage("Connection unresponsive " + connection.getAddress().toString());
				}

				@Override
				public void onHeartbeat(JapsonConnection connection) {}
			});
		} catch (UnknownHostException | SocketException e) {
			e.printStackTrace();
		}
		Metrics metrics = metricsFactory.make(this, 15887);
		metrics.addCustomChart(new SimplePie("amount_of_plugins", () -> getProxy().getPluginManager().getPlugins().size() + ""));
		metrics.addCustomChart(new SimplePie("storage_type", () -> variableManager.getMainStorage().getNames()[0]));
	}

	@Subscribe
	public void onProxyShutdown(ProxyShutdownEvent event) {
		japson.shutdown();
	}

	@Override
	public JapsonServer getJapsonServer() {
		return japson;
	}

	public ProxyServer getProxy() {
		return proxy;
	}

	@Override
	public void consoleMessages(String... messages) {
		for (String message : messages)
			logger.info(message);
	}

	@Override
	public void debugMessage(Exception exception, String... strings) {
		debugMessages(exception, strings);
	}

	@Override
	public void debugMessages(String... messages) {
		for (String message : messages)
			logger.debug(message);
	}

	@Override
	public void debugMessages(Exception exception, String... strings) {
		if (!configuration.isDebug())
			return;
		for (String string : strings)
			consoleMessage("&b" + string);
		exception.printStackTrace();
	}

	public SkungeeAPI getAPI() {
		return API;
	}

	@Override
	public Optional<SkungeeServer> getServer(InetSocketAddress address) {
		Optional<ServerInfo> info = proxy.getAllServers().stream()
				.filter(server -> server.getServerInfo().getAddress().equals(address))
				.map(server -> server.getServerInfo())
				.findFirst();
		if (!info.isPresent())
			return Optional.empty();
		return getServer(info.get());
	}

	@Override
	public Optional<SkungeeServer> getServer(String name) {
		Optional<ServerInfo> info = proxy.getAllServers().stream()
				.filter(server -> server.getServerInfo().getName().equals(name))
				.map(server -> server.getServerInfo())
				.findFirst();
		if (!info.isPresent())
			return Optional.empty();
		return getServer(info.get());
	}

	public Optional<SkungeeServer> getServer(ServerInfo info) {
		Optional<ServerData> dataOptional = serverDataManager.get(info.getAddress());
		if (!dataOptional.isPresent())
			return Optional.empty();
		ServerData data = dataOptional.get();
		boolean online = japson.getConnections().getConnection(data.getJapsonAddress()).isPresent();
		return Optional.of(new SkungeeServer(info.getName(), online, data));
	}

	@Override
	public Set<SkungeeServer> getServers() {
		return proxy.getAllServers().stream()
				.map(server -> server.getServerInfo())
				.map(info -> getServer(info))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet());
	}

	@Override
	public Optional<SkungeePlayer> getPlayer(String name) {
		Optional<Player> player = proxy.getPlayer(name);
		if (!player.isPresent())
			return Optional.empty();
		return Optional.of(new SkungeePlayer(player.get().getUsername(), player.get().getUniqueId(), getCurrentServer(player.get().getUniqueId())));
	}

	@Override
	public Optional<SkungeePlayer> getPlayer(UUID uuid) {
		Optional<Player> player = proxy.getPlayer(uuid);
		if (!player.isPresent())
			return Optional.empty();
		return Optional.of(new SkungeePlayer(player.get().getUsername(), player.get().getUniqueId(), getCurrentServer(player.get().getUniqueId())));
	}

	@Override
	public Set<SkungeePlayer> getPlayers() {
		return proxy.getAllPlayers().stream()
				.map(player -> getPlayer(player.getUniqueId()))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet());
	}

	private String getCurrentServer(UUID uuid) {
		Optional<Player> optional = proxy.getPlayer(uuid);
		if (!optional.isPresent()) // Low chance of happening, unless disconnected?
			return null;
		Optional<ServerConnection> server = optional.get().getCurrentServer();
		if (!server.isPresent())
			return null;
		return server.get().getServerInfo().getName();
	}

	@Override
	public void setApiHandler(Handler handler) throws IllegalAccessException {
		if (handler.getID() != Packets.API.getPacketId())
			throw new IllegalAccessException("The API handler must represent the Packets.API packet ID");
		japson.registerHandlers(handler);
	}

	@Override
	public VelocityConfiguration getPlatformConfiguration() {
		return configuration;
	}

	@Override
	public void schedule(Runnable task, long delay, long period, TimeUnit unit) {
		proxy.getScheduler().buildTask(this, task).repeat(period, unit);
	}

	@Override
	public void delay(Runnable task, long delay, TimeUnit unit) {
		proxy.getScheduler().buildTask(this, task).delay(delay, unit);
	}

	@Override
	public ServerDataManager getServerDataManager() {
		return serverDataManager;
	}

	@Override
	public VariableManager getVariableManager() {
		return variableManager;
	}

	@Override
	public EventManager getEventManager() {
		return eventManager;
	}

	@Override
	public File getScriptsDirectory() {
		return SCRIPTS_FOLDER;
	}

	@Override
	public File getPlatformFolder() {
		return dataFolder;
	}

}
