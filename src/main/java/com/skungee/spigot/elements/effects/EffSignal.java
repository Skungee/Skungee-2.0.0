package com.skungee.spigot.elements.effects;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.sitrica.japson.gson.JsonArray;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Packet;
import com.skungee.shared.Packets;
import com.skungee.shared.objects.SkungeeServer;
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

@Name("Signal")
@Description("Send a signal to another server. Signals act like a messaging system to talk between servers. Use the signal event to listen to the signal for the other servers.")
@Examples("send signal message \"START Skywars\" to server \"Skyblock\"")
@Since("2.0.0")
public class EffSignal extends Effect {

	static {
		Skript.registerEffect(EffSignal.class, "[send] signal[s] [message[s]] %strings% [to %-skungeeservers%]");
	}

	private Expression<SkungeeServer> servers;
	private Expression<String> strings;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		strings = (Expression<String>) exprs[0];
		servers = (Expression<SkungeeServer>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		try {
			SpigotSkungee.getInstance().getJapsonClient().sendPacket(new Packet(Packets.SIGNAL.getPacketId()) {
				@Override
				public JsonObject toJson() {
					JsonObject object = new JsonObject();
					if (servers != null) {
						JsonArray serversArray = new JsonArray();
						for (SkungeeServer server : servers.getArray(event))
							serversArray.add(server.getName());
						object.add("servers", serversArray);
					}
					JsonArray stringsArray = new JsonArray();
					for (String string : strings.getArray(event))
						stringsArray.add(string);
					object.add("strings", stringsArray);
					return object;
				}
			});
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (debug || strings == null)
			return "signal";
		if (servers == null)
			return "signal " + strings.toString(event, debug);
		return "signal " + strings.toString(event, debug) + " to " + servers.toString(event, debug);
	}

}
