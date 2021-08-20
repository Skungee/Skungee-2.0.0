package com.skungee.spigot.elements.effects;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.sitrica.japson.gson.JsonArray;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Packet;
import com.skungee.shared.Packets;
import com.skungee.shared.objects.SkungeePlayer;
import com.skungee.spigot.SpigotSkungee;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Bungee Player Command")
@Description("Force a player to chat or execute a command, add a slash / to make it a command.")
@Since("2.0.0")
public class EffBungeePlayerCommand extends Effect {

	static {
		Skript.registerEffect(EffBungeePlayerCommand.class, "execute [[prox(ied|y)] players] %skungeeplayers% command %strings%", "make [[prox(ied|y)] players] %skungeeplayers% (say|chat) %strings%");
	}

	private Expression<SkungeePlayer> players;
	private Expression<String> commands;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<SkungeePlayer>) exprs[0];
		commands = (Expression<String>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		try {
			SpigotSkungee.getInstance().getJapsonClient().sendPacket(new Packet(Packets.PROXY_PLAYER_COMMAND.getPacketId()) {
				@Override
				public JsonObject toJson() {
					JsonObject object = new JsonObject();
					JsonArray playersArray = new JsonArray();
					for (SkungeePlayer player : players.getArray(event))
						playersArray.add(player.getUniqueId() + "");
					object.add("players", playersArray);
					JsonArray commandsArray = new JsonArray();
					for (String command : commands.getArray(event))
						commandsArray.add(command);
					object.add("commands", commandsArray);
					return object;
				}
			});
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (debug)
			return "execute player command";
		return "execute players " + players.toString(event, debug) + " commands " + commands.toString(event, debug);
	}

}
