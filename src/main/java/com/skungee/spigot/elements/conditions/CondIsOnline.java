package com.skungee.spigot.elements.conditions;

import com.skungee.shared.objects.SkungeeServer;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Server is Online")
@Description("Check if a server is online.")
@Examples({"if server \"hub\" is online:", "\tmessage \"The hub is correctly online\""})
@Since("2.0.0")
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
