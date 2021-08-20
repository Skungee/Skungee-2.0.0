package com.skungee.spigot.elements.expressions;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.skungee.shared.objects.SkungeeServer;
import com.skungee.spigot.managers.ServerManager;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

@Name("Proxy Servers")
@Description("Grab all the proxy servers.")
@Since("2.0.0")
public class ExprProxyServers extends SimpleExpression<SkungeeServer> {

	static {
		Skript.registerExpression(ExprProxyServers.class, SkungeeServer.class, ExpressionType.SIMPLE, "[(all [[of] the]|the)] [prox(ied|y)] servers");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	@Nullable
	protected SkungeeServer[] get(Event event) {
		return ServerManager.getServers().stream().toArray(SkungeeServer[]::new);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends SkungeeServer> getReturnType() {
		return SkungeeServer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "proxy servers";
	}

}
