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
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Server Command")
@Description("Execute a command on another server.")
@Since("2.0.0")
public class EffServerCommand extends Effect {

	static {
		Skript.registerEffect(EffServerCommand.class, "execute command %strings% on %skungeeservers%");
	}

	private Expression<SkungeeServer> servers;
	private Expression<String> commands;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		commands = (Expression<String>) exprs[0];
		servers = (Expression<SkungeeServer>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		try {
			SpigotSkungee.getInstance().getJapsonClient().sendPacket(new Packet(Packets.SERVER_COMMAND.getPacketId()) {
				@Override
				public JsonObject toJson() {
					JsonObject object = new JsonObject();
					JsonArray serversArray = new JsonArray();
					for (SkungeeServer server : servers.getArray(event))
						serversArray.add(server.getName());
					object.add("servers", serversArray);
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
			return "execute command on servers";
		return "execute commands " + commands.toString(event, debug) + " on servers " + servers.toString(event, debug);
	}

}
