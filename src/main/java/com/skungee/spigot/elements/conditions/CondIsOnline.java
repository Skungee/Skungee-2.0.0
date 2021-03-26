package com.skungee.spigot.elements.conditions;

import com.skungee.shared.objects.SkungeeServer;
import com.skungee.spigot.utils.PropertyCondition;

public class CondIsOnline extends PropertyCondition<SkungeeServer> {

	static {
		register(CondIsOnline.class, "online", "skungeeservers");
	}

	@Override
	public boolean check(SkungeeServer server) {
		return server.isOnline();
	}

	@Override
	protected String getPropertyName() {
		return "online";
	}

}
