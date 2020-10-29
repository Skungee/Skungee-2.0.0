package com.skungee.velocity;

import com.moandjiezana.toml.Toml;
import com.skungee.proxy.ProxyConfiguration;

public class VelocityConfiguration implements ProxyConfiguration {

	private final int PORT, INTERVAL, BUFFER_SIZE, VERSION;
	private final String STORAGE_TYPE, ADDRESS, CHARSET;
	private final boolean DEBUG, BACKUPS, MESSAGES;

	public VelocityConfiguration(Toml configuration, long version) {

		VERSION = configuration.getLong("configuration-version", version).intValue();

		Toml configurations = configuration.getTable("configurations");
		ADDRESS = configurations.getString("bind-address", "127.0.0.1");
		PORT = configurations.getLong("port", 8000L).intValue();
		DEBUG = configurations.getBoolean("debug", false);

		Toml variables = configuration.getTable("network-variables");
		STORAGE_TYPE = variables.getString("type", "CSV");

		Toml backups = configuration.getTable("network-variables.backups");
		INTERVAL = backups.getLong("interval-minutes", 120L).intValue();
		MESSAGES = backups.getBoolean("console-messages", true);
		BACKUPS = backups.getBoolean("enabled", true);

		Toml protocol = configuration.getTable("protocol");
		BUFFER_SIZE = protocol.getLong("buffer-size", 1024L).intValue();

		Toml scripts = configuration.getTable("global-scripts");
		CHARSET = scripts.getString("charset", "default");
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
	public String getScriptsCharset() {
		return CHARSET;
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
