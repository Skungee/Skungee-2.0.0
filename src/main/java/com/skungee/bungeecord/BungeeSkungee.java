package com.skungee.bungeecord;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.reflections.Reflections;

import com.google.common.collect.Lists;
import com.sitrica.japson.server.Connections.JapsonConnection;
import com.sitrica.japson.server.JapsonServer;
import com.sitrica.japson.server.Listener;
import com.sitrica.japson.shared.Executor;
import com.sitrica.japson.shared.Handler;
import com.skungee.proxy.ProxyPlatform;
import com.skungee.proxy.ProxySkungee;
import com.skungee.proxy.ServerDataManager;
import com.skungee.proxy.SkungeeAPI;
import com.skungee.proxy.variables.VariableManager;
import com.skungee.shared.Packets;
import com.skungee.shared.objects.ServerData;
import com.skungee.shared.objects.SkungeePlayer;
import com.skungee.shared.objects.SkungeeServer;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeSkungee extends Plugin implements ProxyPlatform {

	private BungeecordConfiguration configuration;
	private ServerDataManager serverDataManager;
	private VariableManager variableManager;
	private static BungeeSkungee instance;
	private JapsonServer japson;
	private File SCRIPTS_FOLDER;
	private SkungeeAPI API;

	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {
		instance = this;
		try {
			ProxySkungee.setPlatform(this);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		serverDataManager = new ServerDataManager(this);
		API = new SkungeeAPI(this);
		if (!getDataFolder().exists())
			getDataFolder().mkdir();
		SCRIPTS_FOLDER = new File(getDataFolder(), File.separator + "scripts");
		if (!SCRIPTS_FOLDER.exists())
			SCRIPTS_FOLDER.mkdir();
		File file = new File(getDataFolder(), "configuration.yml");
		try (InputStream input = getResourceAsStream("configuration.yml")) {
			if (!file.exists())
				Files.copy(input, file.toPath());
			Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
//			if (!"1".equals(configuration.getString("configuration-version", "old"))) { // Update if version changed for configuration.yml
//				new BungeeConfigSaver(instance, "configuration.yml").execute();
//				Files.copy(input, file.toPath());
//				configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
//				consoleMessage("&eThere is a new version. Generating new " + "configuration.yml");
//			}
			this.configuration = new BungeecordConfiguration(configuration, 1);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		try {
			japson = new JapsonServer(configuration.getBindAddress(), configuration.getPort());
			japson.registerHandlers(new Reflections("com.skungee.proxy.handlers", "com.skungee.bungeecord.handlers")
					.getSubTypesOf(Handler.class).stream().filter(clazz -> clazz != Executor.class).map(clazz -> {
						try {
							return clazz.newInstance();
						} catch (InstantiationException | IllegalAccessException e) {
							e.printStackTrace();
							return null;
						}
					}).filter(handler -> handler != null).toArray(Handler[]::new));
			if (configuration.isDebug()) {
				japson.enableDebug();
				if (configuration.getIgnoredDebugPackets().length != 0)
					japson.addIgnoreDebugPackets(configuration.getIgnoredDebugPackets());
			}
			japson.setPacketBufferSize(configuration.getBufferSize());
			List<InetAddress> address = Lists.newArrayList(InetAddress.getLocalHost(), InetAddress.getByName("127.0.0.1"));
			address.addAll(getProxy().getServers().values().stream().map(server -> server.getAddress().getAddress())
					.collect(Collectors.toList()));
			address.addAll(configuration.getWhitelistedAddresses());
			japson.setAllowedAddresses(address.stream().toArray(InetAddress[]::new));
			japson.registerListener(new Listener() {
				@Override
				public void onAcquiredCommunication(JapsonConnection connection) {
					instance.debugMessage("Connection acquired " + connection.getAddress().getHostAddress() + ":" + connection.getPort());
				}

				@Override
				public void onDisconnect(JapsonConnection connection) {
					instance.debugMessage("Connection disconnected " + connection.getAddress().getHostAddress() + ":" + connection.getPort());
				}

				@Override
				public void onForget(JapsonConnection connection) {
					instance.debugMessage("Connection forgotten " + connection.getAddress().getHostAddress() + ":" + connection.getPort());
				}

				@Override
				public void onReacquiredCommunication(JapsonConnection connection) {
					instance.debugMessage("Connection reestablished " + connection.getAddress().getHostAddress() + ":" + connection.getPort());
				}

				@Override
				public void onShutdown() {}

				@Override
				public void onUnresponsive(JapsonConnection connection) {
					instance.debugMessage("Connection unresponsive " + connection.getAddress().getHostAddress() + ":" + connection.getPort());
				}

				@Override
				public void onHeartbeat(JapsonConnection connection) {}
			});
		} catch (UnknownHostException | SocketException e) {
			e.printStackTrace();
		}
		variableManager = new VariableManager(this);
		consoleMessage("Started on " + japson.getAddress().getHostAddress() + ":" + configuration.getPort());
	}

	@Override
	public void onDisable() {
		japson.shutdown();
	}

	public static BungeeSkungee getInstance() {
		return instance;
	}

	@Override
	public JapsonServer getJapsonServer() {
		return japson;
	}

	public SkungeeAPI getAPI() {
		return API;
	}

	@Override
	public void consoleMessages(String... strings) {
		for (String string : strings)
			ProxyServer.getInstance().getLogger().info(ChatColor.translateAlternateColorCodes('&', "&7[&6Skungee&7] " + string));
	}

	public void debugMessages(String... strings) {
		if (!configuration.isDebug())
			return;
		for (String string : strings)
			consoleMessage("&b" + string);
	}

	@Override
	public Optional<SkungeeServer> getServer(InetSocketAddress address) {
		Optional<ServerInfo> info = getProxy().getServers().values().stream()
				.filter(server -> server.getSocketAddress().equals(address))
				.findFirst();
		if (!info.isPresent())
			return Optional.empty();
		return getServer(info.get());
	}

	@Override
	public Optional<SkungeeServer> getServer(String name) {
		Optional<ServerInfo> info = ProxyServer.getInstance().getServers().entrySet().stream()
				.filter(entry -> entry.getKey().equals(name))
				.map(entry -> entry.getValue())
				.findFirst();
		if (!info.isPresent())
			return Optional.empty();
		return getServer(info.get());
	}

	public Optional<SkungeeServer> getServer(ServerInfo info) {
		Optional<ServerData> dataOptional = serverDataManager.get((InetSocketAddress)info.getSocketAddress());
		if (!dataOptional.isPresent())
			return Optional.empty();
		ServerData data = dataOptional.get();
		InetSocketAddress japsonAddress = data.getJapsonAddress();
		boolean online = japson.getConnections().getConnection(japsonAddress.getAddress(), japsonAddress.getPort()).isPresent();
		return Optional.of(new SkungeeServer(info.getName(), online, data));
	}

	@Override
	public Set<SkungeeServer> getServers() {
		return ProxyServer.getInstance().getServers().values().stream()
				.map(info -> getServer(info))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet());
	}

	@Override
	public Optional<SkungeePlayer> getPlayer(String name) {
		ProxiedPlayer player = getProxy().getPlayer(name);
		if (player == null)
			return Optional.empty();
		return Optional.of(new SkungeePlayer(player.getName(), player.getUniqueId(), getCurrentServer(player.getUniqueId())));
	}

	@Override
	public Optional<SkungeePlayer> getPlayer(UUID uuid) {
		ProxiedPlayer player = getProxy().getPlayer(uuid);
		if (player == null)
			return Optional.empty();
		return Optional.of(new SkungeePlayer(player.getName(), player.getUniqueId(), getCurrentServer(player.getUniqueId())));
	}

	@Override
	public Set<SkungeePlayer> getPlayers() {
		return getProxy().getPlayers().stream()
				.map(player -> getPlayer(player.getUniqueId()))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet());
	}

	private String getCurrentServer(UUID uuid) {
		return getProxy().getPlayer(uuid).getServer().getInfo().getName();
	}

	@Override
	public void setApiHandler(Handler handler) throws IllegalAccessException {
		if (handler.getID() != Packets.API.getPacketId())
			throw new IllegalAccessException("The API handler must represent the Packets.API's packet ID");
		japson.registerHandlers(handler);
	}

	@Override
	public BungeecordConfiguration getPlatformConfiguration() {
		return configuration;
	}

	@Override
	public void schedule(Runnable task, long delay, long period, TimeUnit unit) {
		ProxyServer.getInstance().getScheduler().schedule(this, task, delay, period, unit);
	}

	@Override
	public void delay(Runnable task, long delay, TimeUnit unit) {
		ProxyServer.getInstance().getScheduler().schedule(this, task, delay, unit);
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
	public File getScriptsDirectory() {
		return SCRIPTS_FOLDER;
	}

	@Override
	public File getPlatformFolder() {
		return getDataFolder();
	}

}
