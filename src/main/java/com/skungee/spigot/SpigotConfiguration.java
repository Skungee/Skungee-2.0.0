package com.skungee.spigot;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.skungee.shared.PlatformConfiguration;

public class SpigotConfiguration implements PlatformConfiguration {

	private final int PORT, BUFFER_SIZE, VERSION;
	private final ReceiverPorts recevierPorts;
	private final boolean DEBUG, RECEIVER;
	private final String ADDRESS;

	public SpigotConfiguration(FileConfiguration configuration, int version) {
		recevierPorts = new ReceiverPorts(configuration.getConfigurationSection("receiver.ports"));
		VERSION = configuration.getInt("configuration-version", version);
		BUFFER_SIZE = configuration.getInt("protocol.buffer-size", 1024);
		ADDRESS = configuration.getString("bind-address", "127.0.0.1");
		RECEIVER = configuration.getBoolean("receiver.enabled", true);
		DEBUG = configuration.getBoolean("debug", false);
		PORT = configuration.getInt("port", 8000);
	}

	public ReceiverPorts getReceiverPorts() {
		return recevierPorts;
	}

	@Override
	public int getConfigurationVersion() {
		return VERSION;
	}

	@Override
	public String getBindAddress() {
		return ADDRESS;
	}

	public boolean hasReceiver() {
		return RECEIVER;
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

	public class ReceiverPorts {

		private final int START, END, PORT;
		private final boolean AUTO;

		public ReceiverPorts(ConfigurationSection section) {
			this.START = section.getInt("automatic-range.start", 1000);
			this.END = section.getInt("automatic-range.end", 30000);
			this.AUTO = section.getBoolean("automatic", true);
			this.PORT = section.getInt("port", 2000);
		}

		public ReceiverPorts(boolean AUTO, int START, int END, int PORT) {
			this.START = START;
			this.AUTO = AUTO;
			this.PORT = PORT;
			this.END = END;
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
