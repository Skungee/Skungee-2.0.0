package com.skungee.spigot.elements.effects;

import java.util.Arrays;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.sitrica.japson.gson.JsonArray;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Packet;
import com.skungee.shared.Packets;
import com.skungee.shared.objects.SkungeeServer;
import com.skungee.spigot.SpigotSkungee;
import com.skungee.spigot.objects.SkungeePlayerMapper;
import com.skungee.spigot.objects.SkungeeServerMapper;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

public class EffConnect extends Effect {

	static {
		Skript.registerEffect(EffConnect.class, "(connect|send) %players/skungeeplayers/strings/uuids% to [proxy] [server] %skungeeserver/string%");
	}

	private Expression<Object> players, servers;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Object>) exprs[0];
		servers = (Expression<Object>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		SpigotSkungee.getInstance().getJapsonClient().sendPacket(new Packet(Packets.CONNECT.getPacketId()) {
			@Override
			public JsonObject toJson() {
				JsonObject object = new JsonObject();
				JsonArray playersArray = new JsonArray();
				Arrays.stream(players.getArray(event))
						.map(new SkungeePlayerMapper())
						.filter(player -> player != null)
						.forEach(player -> playersArray.add(player.getUniqueId() + ""));
				object.add("players", playersArray);
				SkungeeServer server = new SkungeeServerMapper().apply(servers.getSingle(event));
				if (server != null)
					object.addProperty("server", server.getName());
				return object;
			}
		});
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (debug)
			return "connect players";
		return "connect " + players.toString(event, debug) + " to servers " + servers.toString(event, debug);
	}

}
