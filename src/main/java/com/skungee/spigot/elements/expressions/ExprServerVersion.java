package com.skungee.spigot.elements.expressions;

import org.eclipse.jdt.annotation.Nullable;

import com.skungee.shared.objects.SkungeeServer;
import com.skungee.spigot.objects.SkungeeServerMapper;

import ch.njol.skript.expressions.base.SimplePropertyExpression;

public class ExprServerVersion extends SimplePropertyExpression<Object, String> {

	static {
		register(ExprServerVersion.class, String.class, "version", "skungeeservers/strings");
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
		return server.getVersion();
	}

	@Override
	protected String getPropertyName() {
		return "version";
	}

}
