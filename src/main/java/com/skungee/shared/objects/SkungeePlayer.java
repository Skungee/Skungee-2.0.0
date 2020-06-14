package com.skungee.shared.objects;

import java.util.UUID;

public class SkungeePlayer {

	private final String name, server;
	private final UUID uuid;

	public SkungeePlayer(String name, UUID uuid, String server) {
		this.server = server;
		this.uuid = uuid;
		this.name = name;
	}

	public String getCurrentServer() {
		return server;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public String getName() {
		return name;
	}

}
