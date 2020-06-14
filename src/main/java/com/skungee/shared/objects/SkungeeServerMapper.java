package com.skungee.shared.objects;

import java.util.Optional;
import java.util.function.Function;

import com.skungee.spigot.managers.ServerManager;

public class SkungeeServerMapper implements Function<Object, SkungeeServer> {

	@Override
	public SkungeeServer apply(Object object) {
		if (object instanceof SkungeeServer)
			return (SkungeeServer) object;
		Optional<SkungeeServer> server = ServerManager.getServer((String) object);
		if (!server.isPresent())
			return null;
		return server.get();
	}

}
