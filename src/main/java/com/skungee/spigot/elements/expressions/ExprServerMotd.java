package com.skungee.spigot.elements.expressions;

import org.eclipse.jdt.annotation.Nullable;

import com.skungee.shared.objects.SkungeeServer;

import ch.njol.skript.expressions.base.SimplePropertyExpression;

public class ExprServerMotd extends SimplePropertyExpression<SkungeeServer, String> {

	static {
		register(ExprServerMotd.class, String.class, "(message of the day|motd)", "skungeeservers");
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	@Nullable
	public String convert(SkungeeServer server) {
		return server.getMotd();
	}

	@Override
	protected String getPropertyName() {
		return "motd";
	}

}
