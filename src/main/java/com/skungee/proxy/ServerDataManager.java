package com.skungee.proxy;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class ServerDataManager {

	private final static Map<InetSocketAddress, ServerData> map = new HashMap<>();

	public static ServerData get(InetSocketAddress address) {
		return Optional.ofNullable(map.get(address)).orElseGet(() -> {
			ServerData data = new ServerData();
			map.put(address, data);
			return data;
		});
	}

	public static class ServerData {

		private Set<UUID> whitelisted;
		private String motd, version;
		private int limit;

		public ServerData() {}

		public String getMotd() {
			return motd;
		}

		public void setWhitelisted(Set<UUID> whitelisted) {
			this.whitelisted = whitelisted;
		}

		public Set<UUID> getWhitelisted() {
			return whitelisted;
		}

		public void setMotd(String motd) {
			this.motd = motd;
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
