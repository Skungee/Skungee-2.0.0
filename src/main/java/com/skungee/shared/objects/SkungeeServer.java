package com.skungee.shared.objects;

public class SkungeeServer {

	private final boolean online;
	private final String name;

	public SkungeeServer(String name, boolean online) {
		this.online = online;
		this.name = name;
	}

	public boolean isOnline() {
		return online;
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
