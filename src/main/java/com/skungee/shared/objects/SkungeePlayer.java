package com.skungee.shared.objects;

import java.util.UUID;

public class SkungeePlayer {

	private final String name;
	private final UUID uuid;

	public SkungeePlayer(String name, UUID uuid) {
		this.uuid = uuid;
		this.name = name;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public String getName() {
		return name;
	}

}
