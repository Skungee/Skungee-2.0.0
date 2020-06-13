package com.skungee.spigot;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import com.sitrica.japson.client.JapsonClient;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;

public class SpigotSkungee extends JavaPlugin {

	private static SpigotSkungee instance;
	private JapsonClient japson;
	private SkriptAddon addon;
	private Metrics metrics;

	@Override
	public void onEnable() {
		instance = this;
		File configFile = new File(getDataFolder(), "config.yml");
		//If newer version was found, update configuration.
		int version = 1;
		if (version != getConfig().getInt("configuration-version", version)) {
			if (configFile.exists())
				configFile.delete();
		}
		saveDefaultConfig();
		try {
			japson = new JapsonClient(getConfig().getInt("port", 8000));
			if (getConfig().getBoolean("debug", false))
				japson.enableDebug();
			japson.setPacketBufferSize(getConfig().getInt("protocol.buffer-size", 1024));
			consoleMessage("Started on " + InetAddress.getLocalHost().getHostAddress() + ":" + getConfig().getInt("port", 8000));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		metrics = new Metrics(this);
		try {
			addon = Skript.registerAddon(this)
					.loadClasses("com.skungee.spigot", "elements")
					.setLanguageFileDirectory("lang");
		} catch (IOException e) {
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

	public Metrics getMetrics() {
		return metrics;
	}

	public void consoleMessage(String string) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&6Skungee&7]" + " " + string));
	}

	public void debugMessage(String string) {
		if (getConfig().getBoolean("debug", false))
			consoleMessage("&b" + string);
	}

}
