package com.skungee.proxy;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class ServerDataManager {

	private final static Map<InetSocketAddress, ServerData> map = new HashMap<>();

	/**
	 * Grab a ServerData from the actual defined address in the config.yml of a sevrer.
	 * 
	 * @param address The server of the server to get or make.
	 * @return The ServerData found or generated.
	 */
	public static Optional<ServerData> get(InetSocketAddress address) {
		return Optional.ofNullable(map.get(address));
	}

	public static void set(InetSocketAddress address, ServerData data) {
		map.put(address, data);
	}

	public static class ServerData {

		private final InetSocketAddress japsonAddress, serverAddress;
		private Set<UUID> whitelisted = new HashSet<>();
		private String motd, version;
		private Integer receiverPort;
		private int limit;

		public ServerData(InetSocketAddress serverAddress, InetSocketAddress japsonAddress) {
			this.serverAddress = serverAddress;
			this.japsonAddress = japsonAddress;
		}

		public InetSocketAddress getAddress() {
			return serverAddress;
		}

		public InetSocketAddress getJapsonAddress() {
			return japsonAddress;
		}

		public String getMotd() {
			return motd;
		}

		public void setWhitelisted(Set<UUID> whitelisted) {
			this.whitelisted = whitelisted;
		}

		public Set<UUID> getWhitelisted() {
			return whitelisted;
		}

		public void setReceiverPort(int receiverPort) {
			this.receiverPort = receiverPort;
		}

		public void setMotd(String motd) {
			this.motd = motd;
		}

		public boolean hasReceiver() {
			return receiverPort != null;
		}

		public int getReceiverPort() {
			return receiverPort;
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

		public int getMaxPlayerLimit() {
			return limit;
		}

		public void setMaxPlayerLimit(int limit) {
			this.limit = limit;
		}

	}

}
