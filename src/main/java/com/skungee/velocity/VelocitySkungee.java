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

import org.reflections.Reflections;
import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.sitrica.japson.server.JapsonServer;
import com.sitrica.japson.server.Listener;
import com.sitrica.japson.server.Connections.JapsonConnection;
import com.sitrica.japson.shared.Executor;
import com.sitrica.japson.shared.Handler;
import com.skungee.proxy.ProxyPlatform;
import com.skungee.proxy.ServerDataManager;
import com.skungee.proxy.ServerDataManager.ServerData;
import com.skungee.proxy.variables.VariableManager;
import com.skungee.shared.Packets;
import com.skungee.shared.Skungee;
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
@Plugin(id = "skungee", name = "Skungee", version = "@version@",
		description = "The simplest Skript addon for Bungeecord.", authors = {"Skungee"})
public class VelocitySkungee implements ProxyPlatform {

	private final VelocityConfiguration configuration;
	private final VariableManager variableManager;
	private final ProxyServer proxy;
	private final File dataFolder;
	private final Logger logger;
	private JapsonServer japson;

	@Inject
	public VelocitySkungee(ProxyServer proxy, Logger logger, @DataDirectory Path path) {
		this.proxy = proxy;
		this.logger = logger;
		dataFolder = path.toFile();
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
			Skungee.setPlatform(this);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		variableManager = new VariableManager(this);
		logger.info("SimpleSkungee has been enabled!");
	}

	private final VelocitySkungee getSelf() {
		return this;
	}

	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
		try {
			japson = new JapsonServer(configuration.getBindAddress(), configuration.getPort());
			japson.registerHandlers(new Reflections("com.skungee.proxy.handlers", "com.skungee.velocity.handlers")
					.getSubTypesOf(Handler.class).stream()
					.filter(clazz -> clazz != Executor.class)
					.map(clazz -> {
						try {
							return clazz.getConstructor(ProxyServer.class).newInstance(proxy);
						} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
							e.printStackTrace();
							return null;
						}
					})
					.filter(handler -> handler != null)
					.toArray(Handler[]::new));
			if (configuration.isDebug())
				japson.enableDebug();
			japson.setPacketBufferSize(configuration.getBufferSize());
			List<InetAddress> address = Lists.newArrayList(InetAddress.getLocalHost(), InetAddress.getByName("127.0.0.1"));
			address.addAll(proxy.getAllServers().stream()
					.map(server -> server.getServerInfo().getAddress().getAddress())
					.collect(Collectors.toList()));
			japson.setAllowedAddresses(address.stream().toArray(InetAddress[]::new));
			japson.registerListener(new Listener() {
				@Override
				public void onAcquiredCommunication(JapsonConnection connection) {
					getSelf().debugMessage("Connection acquired " + connection.getAddress().getHostAddress() + ":" + connection.getPort());
				}

				@Override
				public void onDisconnect(JapsonConnection connection) {
					getSelf().debugMessage("Connection disconnected " + connection.getAddress().getHostAddress() + ":" + connection.getPort());
				}

				@Override
				public void onForget(JapsonConnection connection) {
					getSelf().debugMessage("Connection forgotten " + connection.getAddress().getHostAddress() + ":" + connection.getPort());
				}

				@Override
				public void onReacquiredCommunication(JapsonConnection connection) {
					getSelf().debugMessage("Connection reestablished " + connection.getAddress().getHostAddress() + ":" + connection.getPort());
				}

				@Override
				public void onShutdown() {}

				@Override
				public void onUnresponsive(JapsonConnection connection) {
					getSelf().debugMessage("Connection unresponsive " + connection.getAddress().getHostAddress() + ":" + connection.getPort());
				}

				@Override
				public void onHeartbeat(JapsonConnection connection) {}
			});
		} catch (UnknownHostException | SocketException e) {
			e.printStackTrace();
		}
	}

	@Subscribe
	public void onProxyInitialization(ProxyShutdownEvent event) {
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
	public void debugMessages(String... messages) {
		for (String message : messages)
			logger.debug(message);
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
		Optional<ServerData> dataOptional = ServerDataManager.get(info.getAddress());
		if (!dataOptional.isPresent())
			return Optional.empty();
		ServerData data = dataOptional.get();
		InetSocketAddress japsonAddress = data.getJapsonAddress();
		boolean online = japson.getConnections().getConnection(japsonAddress.getAddress(), japsonAddress.getPort()).isPresent();
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
	public VariableManager getVariableManager() {
		return variableManager;
	}

	@Override
	public File getPlatformFolder() {
		return dataFolder;
	}

}
