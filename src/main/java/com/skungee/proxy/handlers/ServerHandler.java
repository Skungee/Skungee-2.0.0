package com.skungee.proxy.handlers;

import java.net.InetAddress;
import java.util.Optional;

import com.sitrica.japson.gson.JsonArray;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Handler;
import com.skungee.proxy.ProxyPlatform;
import com.skungee.proxy.ProxySkungee;
import com.skungee.shared.Packets;
import com.skungee.shared.objects.SkungeeServer;
import com.skungee.shared.serializers.SkungeeServerSerializer;

public class ServerHandler extends Handler {

	private final SkungeeServerSerializer serializer = new SkungeeServerSerializer();

	public ServerHandler() {
		super(Packets.SERVERS.getPacketId());
	}

	@Override
	public JsonObject handle(InetAddress address, int port, JsonObject object) {
		JsonObject returning = new JsonObject();
		ProxyPlatform platform = ProxySkungee.getPlatform();
		if (object.has("name")) {
			Optional<SkungeeServer> server = platform.getServer(object.get("name").getAsString());
			if (server.isPresent())
				returning.add("server", serializer.serialize(server.get(), SkungeeServer.class, null));
			return returning;
		}
		JsonArray array = new JsonArray();
		platform.getServers().forEach(server -> array.add(serializer.serialize(server, SkungeeServer.class, null)));
		returning.add("servers", array);
		return returning;
	}

}
