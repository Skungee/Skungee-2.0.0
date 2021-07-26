package com.skungee.spigot.handlers;

import java.net.InetSocketAddress;

import org.bukkit.Bukkit;

import com.google.common.collect.Streams;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Executor;
import com.skungee.shared.Packets;
import com.skungee.spigot.events.SignalReceiveEvent;

public class SignalHandler extends Executor {

	public SignalHandler() {
		super(Packets.SIGNAL.getPacketId());
	}

	@Override
	public void execute(InetSocketAddress address, JsonObject object) {
		if (!object.has("strings"))
			return;
		String[] strings = Streams.stream(object.get("strings").getAsJsonArray())
				.map(element -> element.getAsString())
				.toArray(String[]::new);
		Bukkit.getPluginManager().callEvent(new SignalReceiveEvent(strings));
	}

}
