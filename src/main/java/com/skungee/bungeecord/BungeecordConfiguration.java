package com.skungee.bungeecord;

import com.skungee.proxy.ProxyConfiguration;

import net.md_5.bungee.config.Configuration;

public class BungeecordConfiguration implements ProxyConfiguration {

	private final int PORT, INTERVAL, BUFFER_SIZE, VERSION;
	private final boolean DEBUG, BACKUPS, MESSAGES;
	private final String STORAGE_TYPE, ADDRESS;

	public BungeecordConfiguration(Configuration configuration, int version) {
		MESSAGES = configuration.getBoolean("network-variables.backups.console-messages", true);
		INTERVAL = configuration.getInt("network-variables.backups.interval-minutes", 120);
		BACKUPS = configuration.getBoolean("network-variables.backups.enabled", true);
		STORAGE_TYPE = configuration.getString("network-variables.type", "CSV");
		BUFFER_SIZE = configuration.getInt("protocol.buffer-size", 1024);
		VERSION = configuration.getInt("configuration-version", version);
		ADDRESS = configuration.getString("bind-address", "127.0.0.1");
		DEBUG = configuration.getBoolean("debug", false);
		PORT = configuration.getInt("port", 8000);
	}

	@Override
	public boolean hasBackupConsoleMessages() {
		return MESSAGES;
	}

	@Override
	public String getVariableDatabaseType() {
		return STORAGE_TYPE;
	}

	@Override
	public long getMinutesBackupInterval() {
		return INTERVAL;
	}

	@Override
	public int getConfigurationVersion() {
		return VERSION;
	}

	@Override
	public boolean isBackupsEnabled() {
		return BACKUPS;
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
