package com.skungee.velocity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.reflections.Reflections;
import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.sitrica.japson.server.JapsonServer;
import com.sitrica.japson.shared.Handler;
import com.skungee.shared.Platform;
import com.skungee.shared.Skungee;
import com.skungee.shared.objects.SkungeePlayer;
import com.skungee.shared.objects.SkungeeServer;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.config.ProxyConfig;
import com.velocitypowered.api.proxy.server.ServerInfo;

/**
 * Velocity
 */
@Plugin(id = "skungee", name = "Skungee", version = "@version@",
        description = "The simplest Skript addon for Bungeecord.", authors = {"Skungee"})
public class VelocitySkungee implements Platform {

	private final Toml configuration;
	private final ProxyServer proxy;
	private final Logger logger;
	private JapsonServer japson;

	@Inject
	public VelocitySkungee(ProxyServer proxy, Logger logger, @DataDirectory Path path) {
		this.proxy = proxy;
		this.logger = logger;
		File folder = path.toFile();
		File file = new File(folder, "config.toml");
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
        configuration = new Toml().read(file);
        try {
			Skungee.setPlatform(this);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		logger.info("SimpleSkungee has been enabled!");
	}

	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
		try {
			japson = new JapsonServer(configuration.getString("bind-address", "0.0.0.0"), configuration.getLong("port", 8000L).intValue());
			japson.registerHandlers(new Reflections("com.skungee.shared.handlers", "com.skungee.velocity.handlers").getSubTypesOf(Handler.class).stream()
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
			if (configuration.getBoolean("debug", false))
				japson.enableDebug();
			japson.setPacketBufferSize(configuration.getLong("protocol.buffer-size", 1024L).intValue());
			List<InetAddress> address = Lists.newArrayList(InetAddress.getLocalHost());
			address.addAll(proxy.getAllServers().stream()
					.map(server -> server.getServerInfo().getAddress().getAddress())
					.collect(Collectors.toList()));
			japson.setAllowedAddresses(address.stream().toArray(InetAddress[]::new));
		} catch (UnknownHostException | SocketException e) {
			e.printStackTrace();
		}
	}

	public void consoleMessage(String string) {
		logger.info(string);
	}

	public ProxyConfig getConfiguration() {
		return null;
	}

	public ProxyServer getProxy() {
		return proxy;
	}

	public void consoleMessage(String... messages) {
		for (String message : messages)
			logger.info(message);
	}

	public void debugMessage(String message) {
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
		return Optional.of(new SkungeeServer(info.get().getName()));
	}

	@Override
	public Set<SkungeeServer> getServers() {
		return proxy.getAllServers().stream()
				.map(server -> new SkungeeServer(server.getServerInfo().getName()))
				.collect(Collectors.toSet());
	}

	@Override
	public Optional<SkungeePlayer> getPlayer(String name) {
		Optional<Player> player = proxy.getPlayer(name);
		if (!player.isPresent())
			return Optional.empty();
		return Optional.of(new SkungeePlayer(player.get().getUsername(), player.get().getUniqueId()));
	}

	@Override
	public Optional<SkungeePlayer> getPlayer(UUID uuid) {
		Optional<Player> player = proxy.getPlayer(uuid);
		if (!player.isPresent())
			return Optional.empty();
		return Optional.of(new SkungeePlayer(player.get().getUsername(), player.get().getUniqueId()));
	}

	@Override
	public Set<SkungeePlayer> getPlayers() {
		return proxy.getAllPlayers().stream()
				.map(player -> getPlayer(player.getUniqueId()))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet());
	}

	@Override
	@Nullable
	public SkungeeServer getCurrentServer(SkungeePlayer player) {
		Optional<Player> optional = proxy.getPlayer(player.getUniqueId());
		if (!optional.isPresent()) // Low chance of happening, unless disconnected?
			return null;
		Optional<ServerConnection> server = optional.get().getCurrentServer();
		if (!server.isPresent())
			return null;
		return new SkungeeServer(server.get().getServerInfo().getName());
	}

}
