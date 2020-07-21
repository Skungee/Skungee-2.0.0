package com.skungee.spigot.handlers;

import java.net.InetAddress;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Handler;
import com.skungee.shared.Packets;

public class SignalHandler extends Handler {

	public SignalHandler() {
		super(Packets.SIGNAL.getPacketId());
	}

	@Override
	public JsonObject handle(InetAddress address, int port, JsonObject object) {
		if (!object.has("strings"))
			return null;
		List<String> strings = Streams.stream(object.get("strings").getAsJsonArray())
				.map(element -> element.getAsString())
				.collect(Collectors.toList());
		// TODO call an event with strings and make an event syntax for signals.
		return null;
	}

}
