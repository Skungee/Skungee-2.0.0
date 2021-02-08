package com.skungee.bungeecord;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.skungee.proxy.ProxyConfiguration;
import com.skungee.shared.Packets;

import net.md_5.bungee.config.Configuration;

public class BungeecordConfiguration implements ProxyConfiguration {

	private final Set<InetAddress> whitelisted = new HashSet<>();
	private final Set<Packets> ignored = new HashSet<>();
	private final int PORT, INTERVAL, BUFFER_SIZE, VERSION;
	private final String STORAGE_TYPE, ADDRESS, CHARSET;
	private final boolean DEBUG, BACKUPS, MESSAGES;

	public BungeecordConfiguration(Configuration configuration, int version) {
		ignored.addAll(configuration.getList("ignored-packets").stream().map(object -> {
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
		whitelisted.addAll(configuration.getStringList("whitelisted-addresses").stream().map(address -> {
			try {
				return InetAddress.getByName(address);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			return null;
		}).filter(address -> address != null).collect(Collectors.toSet()));
		MESSAGES = configuration.getBoolean("network-variables.backups.console-messages", true);
		INTERVAL = configuration.getInt("network-variables.backups.interval-minutes", 120);
		BACKUPS = configuration.getBoolean("network-variables.backups.enabled", true);
		STORAGE_TYPE = configuration.getString("network-variables.type", "CSV");
		CHARSET = configuration.getString("global-scripts.charset", "default");
		BUFFER_SIZE = configuration.getInt("protocol.buffer-size", 1024);
		VERSION = configuration.getInt("configuration-version", version);
		ADDRESS = configuration.getString("bind-address", "127.0.0.1");
		DEBUG = configuration.getBoolean("debug", false);
		PORT = configuration.getInt("port", 8000);
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
	public boolean isBackupsEnabled() {
		return BACKUPS;
	}

	@Override
	public String getScriptsCharset() {
		return CHARSET;
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
