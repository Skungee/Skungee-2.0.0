package com.skungee.spigot.elements.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Lists;
import com.sitrica.japson.gson.JsonArray;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Packet;
import com.skungee.shared.Packets;
import com.skungee.shared.objects.SkungeePlayer;
import com.skungee.spigot.SpigotSkungee;
import com.skungee.spigot.packets.PlayersPacket;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

public class EffMessage extends Effect {

	static {
		Skript.registerEffect(EffMessage.class, "(message|send) %strings% to [(all [[of] the]|the)] prox(ied|y) [players] [%-skungeeplayers%]");
	}

	private Expression<SkungeePlayer> players;
	private Expression<String> strings;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		strings = (Expression<String>) exprs[0];
		players = (Expression<SkungeePlayer>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		List<SkungeePlayer> receivers  = new ArrayList<>();
		try {
			receivers .addAll(players == null ? new PlayersPacket().send() : Lists.newArrayList(players.getArray(event)));
		} catch (TimeoutException | InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		if (receivers .isEmpty())
			return;
		SpigotSkungee.getInstance().getJapsonClient().sendPacket(new Packet(Packets.MESSAGE.getPacketId()) {
			@Override
			public JsonObject toJson() {
				JsonObject object = new JsonObject();
				JsonArray playersArray = new JsonArray();
				for (SkungeePlayer player : receivers)
					playersArray.add(player.getUniqueId() + "");
				object.add("players", playersArray);
				JsonArray stringsArray = new JsonArray();
				for (String string : strings.getArray(event))
					stringsArray.add(string);
				object.add("strings", stringsArray);
				return object;
			}
		});
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (debug || players == null)
			return "message proxied players";
		return "message " + strings.toString(event, debug) + " to " + players.toString(event, debug);
	}

}
