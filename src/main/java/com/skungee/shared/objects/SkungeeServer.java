package com.skungee.shared.objects;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.skungee.proxy.ServerDataManager.ServerData;

public class SkungeeServer {

	private final ServerData data;
	private final boolean online;
	private final String name;

	public SkungeeServer(String name, boolean online, ServerData data) {
		this.online = online;
		this.name = name;
		this.data = data;
	}

	public Set<OfflinePlayer> getWhitelistedPlayers() {
		return data.getWhitelisted().stream()
				.map(uuid -> Bukkit.getOfflinePlayer(uuid))
				.collect(Collectors.toSet());
	}

	public ServerData getServerData() {
		return data;
	}

	public int getMaxPlayerLimit() {
		return data.getMaxPlayerLimit();
	}

	public String getVersion() {
		return data.getVersion();
	}

	public boolean isOnline() {
		return online;
	}

	public String getMotd() {
		return data.getMotd();
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof SkungeeServer))
			return false;
		SkungeeServer other = (SkungeeServer) object;
		return other.name.equals(name);
	}

}
