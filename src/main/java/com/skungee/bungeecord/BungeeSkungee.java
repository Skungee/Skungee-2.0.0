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
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.reflections.Reflections;

import com.google.common.collect.Lists;
import com.sitrica.japson.server.JapsonServer;
import com.sitrica.japson.shared.Handler;
import com.skungee.shared.Platform;
import com.skungee.shared.Skungee;
import com.skungee.shared.objects.SkungeePlayer;
import com.skungee.shared.objects.SkungeeServer;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeSkungee extends Plugin implements Platform {

	private static BungeeSkungee instance;
	private Configuration configuration;
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
			this.configuration = configuration;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		try {
//			String host = configuration.getString("bind-address", "0.0.0.0");
//			if (!host.equalsIgnoreCase("localhost"))
//				japson = new JapsonServer(configuration.getString("bind-address", "0.0.0.0"), configuration.getInt("port", 8000));
//			else
			japson = new JapsonServer(configuration.getInt("port", 8000));
			japson.registerHandlers(new Reflections("com.skungee.shared.handlers", "com.skungee.bungeecord.handlers")
					.getSubTypesOf(Handler.class).stream().map(clazz -> {
						try {
							return clazz.newInstance();
						} catch (InstantiationException | IllegalAccessException e) {
							e.printStackTrace();
							return null;
						}
					}).filter(handler -> handler != null).toArray(Handler[]::new));
			if (configuration.getBoolean("debug", false))
				japson.enableDebug();
			japson.setPacketBufferSize(configuration.getInt("protocol.buffer-size", 1024));
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
		consoleMessage("Started on " + japson.getAddress().getHostAddress() + ":" + configuration.getInt("port", 8000));
	}

	public static BungeeSkungee getInstance() {
		return instance;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public JapsonServer getJapsonServer() {
		return japson;
	}

	public void consoleMessage(String... messages) {
		for (String string : messages)
			getProxy().getConsole().sendMessage(TextComponent
					.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "[Skungee] " + string)));
	}

	@Override
	public Optional<SkungeeServer> getServer(String name) {
		Optional<ServerInfo> info = ProxyServer.getInstance().getServers().entrySet().stream()
				.filter(entry -> entry.getKey().equals(name))
				.map(entry -> entry.getValue())
				.findFirst();
		if (!info.isPresent())
			return Optional.empty();
		return Optional.of(new SkungeeServer(info.get().getName()));
	}

	@Override
	public Set<SkungeeServer> getServers() {
		return ProxyServer.getInstance().getServers().values().stream()
				.map(info -> new SkungeeServer(info.getName()))
				.collect(Collectors.toSet());
	}

	@Override
	public Optional<SkungeePlayer> getPlayer(String name) {
		ProxiedPlayer player = getProxy().getPlayer(name);
		if (player == null)
			return Optional.empty();
		return Optional.of(new SkungeePlayer(player.getName(), player.getUniqueId()));
	}

	@Override
	public Optional<SkungeePlayer> getPlayer(UUID uuid) {
		ProxiedPlayer player = getProxy().getPlayer(uuid);
		if (player == null)
			return Optional.empty();
		return Optional.of(new SkungeePlayer(player.getName(), player.getUniqueId()));
	}

	@Override
	public Set<SkungeePlayer> getPlayers() {
		return getProxy().getPlayers().stream()
				.map(player -> getPlayer(player.getUniqueId()))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet());
	}

	@Override
	@Nullable
	public SkungeeServer getCurrentServer(SkungeePlayer player) {
		return new SkungeeServer(getProxy().getPlayer(player.getUniqueId()).getServer().getInfo().getName());
	}

}
