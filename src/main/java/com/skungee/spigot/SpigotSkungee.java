package com.skungee.spigot;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import com.sitrica.japson.client.JapsonClient;
import com.skungee.shared.Platform;
import com.skungee.shared.Skungee;
import com.skungee.shared.objects.SkungeePlayer;
import com.skungee.spigot.objects.SkungeePlayerMapper;
import com.skungee.spigot.tasks.ServerDataTask;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;

public class SpigotSkungee extends JavaPlugin implements Platform {

	private SpigotConfiguration configuration;
	private static SpigotSkungee instance;
	private static SkungeeAPI API;
	private JapsonClient japson;
	private SkriptAddon addon;
	private Metrics metrics;

	@Override
	public void onEnable() {
		instance = this;
		API = new SkungeeAPI(this);
		File configFile = new File(getDataFolder(), "config.yml");
		//If newer version was found, update configuration.
		int version = 1;
		if (version != getConfig().getInt("configuration-version", version)) {
			if (configFile.exists())
				configFile.delete();
		}
		saveDefaultConfig();
		configuration = new SpigotConfiguration(getConfig(), version);
		try {
			japson = new JapsonClient(configuration.getBindAddress(), configuration.getPort());
			if (configuration.isDebug())
				japson.enableDebug();
			japson.setPacketBufferSize(configuration.getBufferSize());
			consoleMessage("Started on " + InetAddress.getLocalHost().getHostAddress() + ":" + configuration.getPort());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		metrics = new Metrics(this);
		Bukkit.getScheduler().runTaskTimerAsynchronously(this, new ServerDataTask(japson), 0, 5 * (60 * 20)); // 5 minutes.
		if (Bukkit.getPluginManager().isPluginEnabled("Skript")) {
			try {
				addon = Skript.registerAddon(this)
						.loadClasses("com.skungee.spigot", "elements")
						.setLanguageFileDirectory("lang");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			Skungee.setPlatform(this);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		consoleMessage("has been enabled!");
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

	public static SkungeeAPI getAPI() {
		return API;
	}

	@Override
	public File getPlatformFolder() {
		return getDataFolder();
	}

	public Metrics getMetrics() {
		return metrics;
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
