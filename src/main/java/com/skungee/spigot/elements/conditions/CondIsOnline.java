package com.skungee.spigot.elements.conditions;

import com.skungee.shared.objects.SkungeeServer;

import ch.njol.skript.conditions.base.PropertyCondition;

public class CondIsOnline extends PropertyCondition<SkungeeServer> {

	static {
		register(CondIsOnline.class, PropertyType.BE, "online", "[server[s]] %skungeeservers%");
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
