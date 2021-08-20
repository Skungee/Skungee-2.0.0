package com.skungee.spigot.elements.expressions;

import java.util.Arrays;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.skungee.shared.objects.SkungeeServer;
import com.skungee.spigot.objects.SkungeeServerMapper;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

@Name("Server")
@Description("Grab a server by name or none if not present.")
@Since("2.0.0")
public class ExprServer extends SimpleExpression<SkungeeServer> {

	static {
		Skript.registerExpression(ExprServer.class, SkungeeServer.class, ExpressionType.SIMPLE, "[proxy] server[s] %strings%");
	}

	private Expression<String> names;

	@Override
	public Class<? extends SkungeeServer> getReturnType() {
		return SkungeeServer.class;
	}

	@Override
	public boolean isSingle() {
		return names.isSingle();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		names = (Expression<String>) exprs[0];
		return true;
	}

	@Override
	@Nullable
	protected SkungeeServer[] get(Event event) {
		return Arrays.stream(names.getArray(event)).map(new SkungeeServerMapper()).toArray(SkungeeServer[]::new);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (debug)
			return "get server";
		return "servers " + names.toString(event, debug);
	}

}
