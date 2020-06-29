package com.skungee.spigot.elements.expressions;

import java.util.Optional;

import org.eclipse.jdt.annotation.Nullable;

import com.skungee.shared.objects.SkungeePlayer;
import com.skungee.shared.objects.SkungeeServer;
import com.skungee.spigot.managers.ServerManager;
import com.skungee.spigot.objects.SkungeePlayerMapper;

import ch.njol.skript.expressions.base.SimplePropertyExpression;

public class ExprPlayerServer extends SimplePropertyExpression<Object, SkungeeServer> {

	static {
		register(ExprPlayerServer.class, SkungeeServer.class, "[(current|connected)] server[s]", "skungeeplayers/strings/uuids/offlineplayers");
	}

	@Override
	public Class<? extends SkungeeServer> getReturnType() {
		return SkungeeServer.class;
	}

	@Override
	@Nullable
	public SkungeeServer convert(Object object) {
		SkungeePlayer player = new SkungeePlayerMapper().apply(object);
		if (player == null)
			return null;
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
