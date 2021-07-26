package com.skungee.velocity;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.moandjiezana.toml.Toml;
import com.skungee.proxy.ProxyConfiguration;
import com.skungee.shared.Packets;

public class VelocityConfiguration implements ProxyConfiguration {

	private final Set<InetAddress> whitelisted = new HashSet<>();
	private final Set<Packets> ignored = new HashSet<>();
	private final int INTERVAL, BUFFER_SIZE, VERSION;
	private final boolean DEBUG, BACKUPS, MESSAGES;
	private final InetSocketAddress ADDRESS;
	private final String STORAGE_TYPE;
	private String CHARSET;

	public VelocityConfiguration(Toml configuration, long version) {

		VERSION = configuration.getLong("configuration-version", version).intValue();

		Toml configurations = configuration.getTable("configurations");
		ADDRESS = InetSocketAddress.createUnresolved(configurations.getString("bind-address", "127.0.0.1"), configurations.getLong("port", 8000L).intValue());
		DEBUG = configurations.getBoolean("debug", false);
		if (configurations.getList("ignored-packets") != null)
			ignored.addAll(configurations.getList("ignored-packets").stream().map(object -> {
				if (object instanceof String) {
					Packets found = Packets.valueOf((String) object);
					if (found != null)
						return found;
				} else if (object instanceof Number) {
					for (Packets packet : Packets.values()) {
						if (packet.getPacketId() == ((Number)object).intValue())
							return packet;
					}
				}
				return null;
			}).filter(packet -> packet != null).collect(Collectors.toSet()));

		if (configurations.getList("whitelisted-addresses") != null)
			whitelisted.addAll(configurations.getList("whitelisted-addresses").stream().map(address -> {
				if (!(address instanceof String))
					return null;
				try {
					return InetAddress.getByName((String)address);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				return null;
			}).filter(address -> address != null).collect(Collectors.toSet()));

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
		if (CHARSET.equals("default"))
			CHARSET = "UTF-8";
	}

	@Override
	public Set<InetAddress> getWhitelistedAddresses() {
		return whitelisted;
	}

	@Override
	public Integer[] getIgnoredDebugPackets() {
		return ignored.stream().map(packet -> packet.getPacketId()).toArray(Integer[]::new);
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
	public InetSocketAddress getBindAddress() {
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

}
