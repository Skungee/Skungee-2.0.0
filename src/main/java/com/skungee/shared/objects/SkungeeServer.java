package com.skungee.shared.objects;

public class SkungeeServer {

	private final String name;

	public SkungeeServer(String name) {
		this.name = name;
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
