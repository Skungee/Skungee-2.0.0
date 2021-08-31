package com.skungee.spigot;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.skungee.shared.Packets;
import com.skungee.shared.PlatformConfiguration;
import com.skungee.spigot.utils.Utils;

public class SpigotConfiguration implements PlatformConfiguration {

	private final Set<Packets> ignored = new HashSet<>();
	private final InetSocketAddress ADDRESS;
	private final int BUFFER_SIZE, VERSION;
	private InetSocketAddress RECEIVER;
	private final String CHARSET;
	private final boolean DEBUG;

	public SpigotConfiguration(FileConfiguration configuration, int version) {
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
		if (configuration.getBoolean("receiver.enabled", true)) {
			ReceiverPorts ports = new ReceiverPorts(configuration.getConfigurationSection("receiver.ports"));
			int port = ports.isAutomatic() ? Utils.findPort(ports.getStartingPort(), ports.getEndingPort()) : ports.getPort();
			RECEIVER = new InetSocketAddress(configuration.getString("receiver.bind-address"), port);
		}
		CHARSET = configuration.getString("global-scripts.charset", "default");
		VERSION = configuration.getInt("configuration-version", version);
		BUFFER_SIZE = configuration.getInt("protocol.buffer-size", 1024);
		ADDRESS = new InetSocketAddress(configuration.getString("bind-address", "127.0.0.1"), configuration.getInt("port", 8000));
		DEBUG = configuration.getBoolean("debug", false);
	}

	@Override
	public Integer[] getIgnoredDebugPackets() {
		return ignored.stream().map(packet -> packet.getPacketId()).toArray(Integer[]::new);
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
	public InetSocketAddress getBindAddress() {
		return ADDRESS;
	}

	public InetSocketAddress getReceiverAddress() {
		return RECEIVER;
	}

	public boolean hasReceiver() {
		return RECEIVER != null;
	}

	@Override
	public int getBufferSize() {
		return BUFFER_SIZE;
	}

	@Override
	public boolean isDebug() {
		return DEBUG;
	}

	private class ReceiverPorts {

		private final int START, END, PORT;
		private final boolean AUTO;

		public ReceiverPorts(ConfigurationSection section) {
			this.START = section.getInt("automatic-range.start", 1000);
			this.END = section.getInt("automatic-range.end", 30000);
			this.AUTO = section.getBoolean("automatic", true);
			this.PORT = section.getInt("port", 2000);
		}

		public boolean isAutomatic() {
			return AUTO;
		}

		public int getStartingPort() {
			return START;
		}

		public int getEndingPort() {
			return END;
		}

		public int getPort() {
			return PORT;
		}

	}

}
