package com.skungee.spigot;

import org.bukkit.configuration.file.FileConfiguration;

import com.skungee.shared.PlatformConfiguration;

public class SpigotConfiguration implements PlatformConfiguration {

	private final int PORT, BUFFER_SIZE, VERSION;
	private final String ADDRESS;
	private final boolean DEBUG;

	public SpigotConfiguration(FileConfiguration configuration, int version) {
		VERSION = configuration.getInt("configuration-version", version);
		BUFFER_SIZE = configuration.getInt("protocol.buffer-size", 1024);
		ADDRESS = configuration.getString("bind-address", "127.0.0.1");
		DEBUG = configuration.getBoolean("debug", false);
		PORT = configuration.getInt("port", 8000);
	}

	@Override
	public int getConfigurationVersion() {
		return VERSION;
	}

	@Override
	public String getBindAddress() {
		return ADDRESS;
	}

	@Override
	public int getBufferSize() {
		return BUFFER_SIZE;
	}

	@Override
	public boolean isDebug() {
		return DEBUG;
	}

	@Override
	public int getPort() {
		return PORT;
	}

}
