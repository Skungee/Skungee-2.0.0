package com.skungee.spigot.elements.expressions;

import org.eclipse.jdt.annotation.Nullable;

import com.skungee.shared.objects.SkungeeServer;
import com.skungee.spigot.objects.SkungeeServerMapper;

import ch.njol.skript.expressions.base.SimplePropertyExpression;

public class ExprServerMotd extends SimplePropertyExpression<Object, String> {

	static {
		register(ExprServerMotd.class, String.class, "(message of the day|motd)", "skungeeservers/strings");
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	@Nullable
	public String convert(Object object) {
		SkungeeServer server = new SkungeeServerMapper().apply(object);
		if (server == null)
			return null;
		return server.getMotd();
	}

	@Override
	protected String getPropertyName() {
		return "motd";
	}

}
