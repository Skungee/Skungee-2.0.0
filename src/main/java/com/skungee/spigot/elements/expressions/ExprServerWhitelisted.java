package com.skungee.spigot.elements.expressions;

import java.util.Arrays;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.skungee.shared.objects.SkungeeServer;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

public class ExprServerWhitelisted extends SimpleExpression<OfflinePlayer> {

	static {
		Skript.registerExpression(ExprServerWhitelisted.class, OfflinePlayer.class, ExpressionType.PROPERTY, 
				"[(all [[of] the]|the)] whitelisted players of [server[s]] %skungeeservers%",
				"[(all [[of] the]|the)] [server[s]] %skungeeservers%'[s] whitelisted players");
	}

	@Nullable
	private Expression<SkungeeServer> servers;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		servers = (Expression<SkungeeServer>) exprs[0];
		return true;
	}

	@Override
	@Nullable
	protected OfflinePlayer[] get(Event event) {
		return Arrays.stream(servers.getArray(event))
				.map(server -> server.getWhitelistedPlayers())
				.flatMap(whitelisted -> whitelisted.stream())
				.toArray(OfflinePlayer[]::new);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends OfflinePlayer> getReturnType() {
		return OfflinePlayer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (event == null) // Skript Debug
			return "whitelisted players";
		return "whitelisted players" + servers != null ? " on " + servers.getArray(event) : "";
	}

}
