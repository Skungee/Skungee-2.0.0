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
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Proxy Actionbar")
@Description("Send an actionbar to any player on the proxy.")
@Examples({"send actionbar \"example\" to all proxied players", "send actionbar \"example\" to proxied player uuid of {somevariable}"})
@Since("2.0.0")
public class EffActionbar extends Effect {

	static {
		Skript.registerEffect(EffActionbar.class, "(send|display|show) [an] action[ ]bar [with [(text|message)]] %string% to [prox(ied|y)] [(player|uuid)[s]] %skungeeplayers%");
	}

	private Expression<SkungeePlayer> players;
	private Expression<String> message;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		message = (Expression<String>) exprs[0];
		players = (Expression<SkungeePlayer>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (message == null || players == null)
			return;
		try {
			SpigotSkungee.getInstance().getJapsonClient().sendPacket(new Packet(Packets.ACTIONBAR.getPacketId()) {
				@Override
				public JsonObject toJson() {
					JsonObject object = new JsonObject();
					JsonArray playersArray = new JsonArray();
					for (SkungeePlayer player : players.getArray(event))
						playersArray.add(player.getUniqueId() + "");
					object.add("players", playersArray);
					String string = message.getSingle(event);
					if (string != null)
						object.addProperty("message", string);
					return object;
				}
			});
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (debug)
			return "actionbar";
		return "actionbar " + message.toString(event, debug) + " to " + players.toString(event, debug);
	}

}
