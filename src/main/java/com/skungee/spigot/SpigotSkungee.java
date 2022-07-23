package com.skungee.spigot;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import com.sitrica.japson.client.JapsonClient;
import com.sitrica.japson.server.JapsonServer;
import com.sitrica.japson.shared.Executor;
import com.sitrica.japson.shared.Handler;
import com.skungee.shared.Platform;
import com.skungee.shared.objects.SkungeePlayer;
import com.skungee.spigot.objects.SkungeePlayerMapper;
import com.skungee.spigot.tasks.ServerDataTask;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;

public class SpigotSkungee extends JavaPlugin implements Platform {

	private SpigotConfiguration configuration;
	private static SpigotSkungee instance;
	private JapsonServer receiver;
	private ServerDataTask task;
	private JapsonClient japson;
	private SkriptAddon addon;
	private SkungeeAPI API;

	@Override
	public void onEnable() {
		instance = this;
		File configFile = new File(getDataFolder(), "config.yml");
		//If newer version was found, update configuration.
		int version = 2;
		if (version != getConfig().getInt("configuration-version", version)) {
			if (configFile.exists())
				configFile.delete();
		}
		saveDefaultConfig();
		configuration = new SpigotConfiguration(getConfig(), version);
		japson = new JapsonClient(configuration.getBindAddress());
		if (configuration.isDebug()) {
			japson.enableDebug();
			if (configuration.getIgnoredDebugPackets().length != 0)
				japson.addIgnoreDebugPackets(configuration.getIgnoredDebugPackets());
		}
		japson.makeSureConnectionValid();
		japson.setPacketBufferSize(configuration.getBufferSize());
		japson.start();
		consoleMessage("Started on " + configuration.getBindAddress().toString());
		if (configuration.hasReceiver()) {
			try {
				receiver = new JapsonServer(configuration.getReceiverAddress());
				if (configuration.isDebug())
					receiver.enableDebug();
				receiver.setPacketBufferSize(configuration.getBufferSize());
				receiver.registerHandlers(new Reflections("com.skungee.spigot.handlers")
						.getSubTypesOf(Handler.class).stream().filter(clazz -> !clazz.equals(Executor.class)).map(clazz -> {
							try {
								return clazz.getConstructor().newInstance();
							} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
								e.printStackTrace();
								return null;
							}
						}).filter(handler -> handler != null).toArray(Handler[]::new));
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
		Metrics metrics = new Metrics(this, 1914);
		metrics.addCustomChart(new SimplePie("skript_version", () -> Skript.getVersion().toString()));
		task = new ServerDataTask(this, japson);
		Bukkit.getScheduler().runTaskTimerAsynchronously(this, task, 0, 60 * 20); // 1 minute.
		if (Bukkit.getPluginManager().isPluginEnabled("Skript")) {
			try {
				addon = Skript.registerAddon(this)
						.loadClasses("com.skungee.spigot", "elements")
						.setLanguageFileDirectory("lang");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		API = new SkungeeAPI(this);
		consoleMessage("has been enabled!");
	}

	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
		japson.shutdown();
	}

	/**
	 * Grab the receiever, only present if defined in the configuration.
	 * 
	 * @return Optional<JapsonServer> assuming that the configuration states it's enabled.
	 */
	public Optional<JapsonServer> getReceiver() {
		return Optional.ofNullable(receiver);
	}

	public static SpigotSkungee getInstance() {
		return instance;
	}

	public JapsonClient getJapsonClient() {
		return japson;
	}

	public SkriptAddon getAddonInstance() {
		return addon;
	}

	public SkungeeAPI getAPI() {
		return API;
	}

	@Override
	public File getPlatformFolder() {
		return getDataFolder();
	}

	@Override
	public void consoleMessages(String... strings) {
		for (String string : strings)
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&6Skungee&7] " + string));
	}

	public void debugMessages(String... strings) {
		if (!configuration.isDebug())
			return;
		for (String string : strings)
			consoleMessage("&b" + string);
	}

	@Override
	public void debugMessage(Exception exception, String... strings) {
		debugMessages(exception, strings);
	}

	@Override
	public void debugMessages(Exception exception, String... strings) {
		if (!configuration.isDebug())
			return;
		for (String string : strings)
			consoleMessage("&b" + string);
		exception.printStackTrace();
	}

	@Override
	public SpigotConfiguration getPlatformConfiguration() {
		return configuration;
	}

	@Override
	public Optional<SkungeePlayer> getPlayer(String name) {
		return getPlayers().stream()
				.filter(player -> player.getName().equalsIgnoreCase(name))
				.findFirst();
	}

	@Override
	public Optional<SkungeePlayer> getPlayer(UUID uuid) {
		return getPlayers().stream()
				.filter(player -> player.getUniqueId().equals(uuid))
				.findFirst();
	}

	@Override
	public Set<SkungeePlayer> getPlayers() {
		return Bukkit.getOnlinePlayers().stream()
				.map(new SkungeePlayerMapper())
				.collect(Collectors.toSet());
	}

	@Override
	public void schedule(Runnable task, long delay, long period, TimeUnit unit) {
		Bukkit.getScheduler().runTaskTimer(this, task, delay, unit.toSeconds(period) * 20);
	}

	@Override
	public void delay(Runnable task, long delay, TimeUnit unit) {
		Bukkit.getScheduler().runTaskLater(this, task, delay);
	}

}
