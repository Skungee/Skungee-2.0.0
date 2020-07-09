package com.skungee.proxy.handlers;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Handler;
import com.skungee.proxy.ServerDataManager;
import com.skungee.proxy.ServerDataManager.ServerData;
import com.skungee.shared.Packets;

public class ServerDataHandler extends Handler {

	public ServerDataHandler() {
		super(Packets.SERVER_DATA.getPacketId());
	}

	@Override
	public JsonObject handle(InetAddress address, int port, JsonObject object) {
		if (!object.has("port"))
			throw new IllegalStateException("JsonObject for ServerDataHandler did not contain 'port'");
		if (!object.has("motd"))
			throw new IllegalStateException("JsonObject for ServerDataHandler did not contain 'motd'");
		if (!object.has("limit"))
			throw new IllegalStateException("JsonObject for ServerDataHandler did not contain 'limit'");
		if (!object.has("version"))
			throw new IllegalStateException("JsonObject for ServerDataHandler did not contain 'version'");
		if (!object.has("whitelisted"))
			throw new IllegalStateException("JsonObject for ServerDataHandler did not contain 'whitelisted'");
		ServerData data = ServerDataManager.get(new InetSocketAddress(address, object.get("port").getAsInt()));
		data.setMaxPlayerLimit(object.get("limit").getAsInt());
		data.setVersion(object.get("version").getAsString());
		data.setMotd(object.get("motd").getAsString());
		data.setWhitelisted(Streams.stream(object.get("whitelisted").getAsJsonArray())
				.map(element -> element.getAsString())
				.map(string -> UUID.fromString(string))
				.filter(uuid -> uuid != null)
				.collect(Collectors.toSet()));
		return null;
	}

}
