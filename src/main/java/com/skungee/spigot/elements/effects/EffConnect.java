package com.skungee.spigot.elements.effects;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.sitrica.japson.gson.JsonArray;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Packet;
import com.skungee.shared.Packets;
import com.skungee.shared.objects.SkungeePlayer;
import com.skungee.shared.objects.SkungeeServer;
import com.skungee.spigot.SpigotSkungee;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

public class EffConnect extends Effect {

	static {
		Skript.registerEffect(EffConnect.class, "(connect|send) [[prox(ied|y)] players] %skungeeplayers% to [proxy] [server] %skungeeserver%");
	}

	private Expression<SkungeePlayer> players;
	private Expression<SkungeeServer> server;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<SkungeePlayer>) exprs[0];
		server = (Expression<SkungeeServer>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		SpigotSkungee.getInstance().getJapsonClient().sendPacket(new Packet(Packets.CONNECT.getPacketId()) {
			@Override
			public JsonObject toJson() {
				JsonObject object = new JsonObject();
				JsonArray playersArray = new JsonArray();
				for (SkungeePlayer player : players.getArray(event))
					playersArray.add(player.getUniqueId() + "");
				object.add("players", playersArray);
				SkungeeServer found = server.getSingle(event);
				if (found != null)
					object.addProperty("server", found.getName());
				return object;
			}
		});
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (debug)
			return "connect players";
		return "connect " + players.toString(event, debug) + " to server " + server.toString(event, debug);
	}

}
