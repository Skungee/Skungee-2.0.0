package com.skungee.bungeecord;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
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
import com.sitrica.japson.server.JapsonServer;
import com.sitrica.japson.shared.Handler;
import com.skungee.proxy.ProxyPlatform;
import com.skungee.proxy.ServerDataManager;
import com.skungee.proxy.variables.VariableManager;
import com.skungee.shared.Packets;
import com.skungee.shared.Skungee;
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
	private VariableManager variableManager;
	private static BungeeSkungee instance;
	private JapsonServer japson;

	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {
		instance = this;
		if (!getDataFolder().exists())
			getDataFolder().mkdir();
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
					.getSubTypesOf(Handler.class).stream().map(clazz -> {
						try {
							return clazz.newInstance();
						} catch (InstantiationException | IllegalAccessException e) {
							e.printStackTrace();
							return null;
						}
					}).filter(handler -> handler != null).toArray(Handler[]::new));
			if (configuration.isDebug())
				japson.enableDebug();
			japson.setPacketBufferSize(configuration.getBufferSize());
			List<InetAddress> address = Lists.newArrayList(InetAddress.getLocalHost());
			address.addAll(getProxy().getServers().values().stream().map(server -> server.getAddress().getAddress())
					.collect(Collectors.toList()));
			japson.setAllowedAddresses(address.stream().toArray(InetAddress[]::new));
		} catch (UnknownHostException | SocketException e) {
			e.printStackTrace();
		}
		try {
			Skungee.setPlatform(this);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		variableManager = new VariableManager(this);
		consoleMessage("Started on " + japson.getAddress().getHostAddress() + ":" + configuration.getPort());
	}

	public static BungeeSkungee getInstance() {
		return instance;
	}

	public JapsonServer getJapsonServer() {
		return japson;
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

	@SuppressWarnings("deprecation")
	@Override
	public Optional<SkungeeServer> getServer(String name) {
		Optional<ServerInfo> optional = ProxyServer.getInstance().getServers().entrySet().stream()
				.filter(entry -> entry.getKey().equals(name))
				.map(entry -> entry.getValue())
				.findFirst();
		if (!optional.isPresent())
			return Optional.empty();
		ServerInfo info = optional.get();
		boolean online = japson.getConnections().getConnection(info.getAddress().getAddress(), info.getAddress().getPort()).isPresent();
		return Optional.of(new SkungeeServer(info.getName(), online, ServerDataManager.get(info.getAddress())));
	}

	@SuppressWarnings("deprecation")
	@Override
	public Set<SkungeeServer> getServers() {
		return ProxyServer.getInstance().getServers().values().stream()
				.map(info -> {
					boolean online = japson.getConnections().getConnection(info.getAddress().getAddress(), info.getAddress().getPort()).isPresent();
					return new SkungeeServer(info.getName(), online, ServerDataManager.get(info.getAddress()));
				})
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
	public VariableManager getVariableManager() {
		return variableManager;
	}

	@Override
	public File getPlatformFolder() {
		return getDataFolder();
	}

}
