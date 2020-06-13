package com.skungee.spigot.elements.expressions;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.skungee.shared.objects.SkungeePlayer;
import com.skungee.shared.objects.SkungeeServer;
import com.skungee.spigot.SpigotSkungee;
import com.skungee.spigot.managers.ServerManager;
import com.skungee.spigot.packets.PlayersPacket;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

public class ExprProxyPlayers extends SimpleExpression<SkungeePlayer> {

	static {
		Skript.registerExpression(ExprProxyPlayers.class, SkungeePlayer.class, ExpressionType.SIMPLE, "[(all [[of] the]|the)] prox(ied|y) players [o(f|n) server[s] %-skungeeservers/strings%]");
	}

	@Nullable
	private Expression<Object> servers;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		servers = (Expression<Object>) exprs[0];
		return true;
	}

	@Override
	@Nullable
	protected SkungeePlayer[] get(Event event) {
		PlayersPacket packet = new PlayersPacket();
		if (servers != null) {
			packet.setServers(Arrays.stream(servers.getArray(event))
					.map(object -> {
						if (object instanceof SkungeeServer)
							return (SkungeeServer) object;
						Optional<SkungeeServer> server = ServerManager.getServer((String) object);
						if (!server.isPresent())
							return null;
						return server.get();
					})
					.filter(server -> server != null)
					.toArray(SkungeeServer[]::new));
		}
		try {
			return SpigotSkungee.getInstance().getJapsonClient().sendPacket(packet).stream()
					.toArray(SkungeePlayer[]::new);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			return null;
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends SkungeePlayer> getReturnType() {
		return SkungeePlayer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (event == null) // Skript Debug
			return "proxied players";
		return "proxied players" + servers != null ? " on " + servers.getArray(event) : "";
	}

}
