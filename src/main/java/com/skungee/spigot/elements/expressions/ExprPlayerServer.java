package com.skungee.spigot.elements.expressions;

import java.util.Optional;

import org.eclipse.jdt.annotation.Nullable;

import com.skungee.shared.objects.SkungeePlayer;
import com.skungee.shared.objects.SkungeeServer;
import com.skungee.spigot.managers.ServerManager;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Player Server")
@Description("Grabs the name of the server the defined players are on.")
@Since("2.0.0")
public class ExprPlayerServer extends SimplePropertyExpression<SkungeePlayer, SkungeeServer> {

	static {
		register(ExprPlayerServer.class, SkungeeServer.class, "[(current|connected)] server[s]", "skungeeplayers");
	}

	@Override
	public Class<? extends SkungeeServer> getReturnType() {
		return SkungeeServer.class;
	}

	@Override
	@Nullable
	public SkungeeServer convert(SkungeePlayer player) {
		Optional<SkungeeServer> server = ServerManager.getServer(player.getCurrentServer());
		if (!server.isPresent())
			return null;
		return server.get();
	}

	@Override
	protected String getPropertyName() {
		return "connected server";
	}

}
