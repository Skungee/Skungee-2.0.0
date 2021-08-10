package com.skungee.spigot.handlers;

import java.net.InetSocketAddress;

import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Executor;
import com.skungee.shared.Packets;

public class EventHandler extends Executor {

	public EventHandler() {
		super(Packets.EVENT.getPacketId());
	}

	@Override
	public void execute(InetSocketAddress address, JsonObject object) {
		if (!object.has("event"))
			return;
		String event = object.get("event").getAsString();
		switch (event) {
//			case "pre login":
//				assert object.has("name") : "Pre login event did not contain a player name.";
//				Bukkit.getPluginManager().callEvent(new SignalReceiveEvent(strings));
		}
	}

}
