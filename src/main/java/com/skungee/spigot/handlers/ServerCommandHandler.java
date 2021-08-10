package com.skungee.spigot.handlers;

import java.net.InetSocketAddress;

import org.bukkit.Bukkit;

import com.google.common.collect.Streams;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Executor;
import com.skungee.shared.Packets;

public class ServerCommandHandler extends Executor {

	public ServerCommandHandler() {
		super(Packets.SERVER_COMMAND.getPacketId());
	}

	@Override
	public void execute(InetSocketAddress address, JsonObject object) {
		if (!object.has("commands"))
			return;
		Streams.stream(object.get("commands").getAsJsonArray())
				.map(element -> element.getAsString())
				.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
	}

}
