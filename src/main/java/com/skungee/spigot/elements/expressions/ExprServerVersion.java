package com.skungee.spigot.elements.expressions;

import org.eclipse.jdt.annotation.Nullable;

import com.skungee.shared.objects.SkungeeServer;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Server Version")
@Description("Grabs the version of servers on the proxy.")
@Since("2.0.0")
public class ExprServerVersion extends SimplePropertyExpression<SkungeeServer, String> {

	static {
		register(ExprServerVersion.class, String.class, "version", "skungeeservers");
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	@Nullable
	public String convert(SkungeeServer server) {
		return server.getVersion();
	}

	@Override
	protected String getPropertyName() {
		return "version";
	}

}
