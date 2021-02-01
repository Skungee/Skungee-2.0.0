package com.skungee.spigot.elements.expressions;

import java.util.Arrays;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.skungee.spigot.objects.SkungeeServerMapper;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

public class ExprServer extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprServer.class, String.class, ExpressionType.SIMPLE, "[proxy] server %strings%");
	}

	private Expression<String> names;

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
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
	protected String[] get(Event event) {
		SkungeeServerMapper mapper = new SkungeeServerMapper();
		return Arrays.stream(names.getArray(event)).map(name -> mapper.apply(name)).toArray(String[]::new);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (debug)
			return "get server";
		return "servers " + names.toString(event, debug);
	}

}
