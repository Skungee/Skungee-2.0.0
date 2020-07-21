package com.skungee.proxy.handlers;

import java.net.InetAddress;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;
import com.sitrica.japson.gson.JsonArray;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.server.JapsonServer;
import com.sitrica.japson.shared.Handler;
import com.sitrica.japson.shared.Packet;
import com.skungee.proxy.ProxyPlatform;
import com.skungee.proxy.ServerDataManager.ServerData;
import com.skungee.shared.Packets;
import com.skungee.shared.Skungee;
import com.skungee.shared.objects.SkungeeServer;

public class SignalHandler extends Handler {

	public SignalHandler() {
		super(Packets.SIGNAL.getPacketId());
	}

	@Override
	public JsonObject handle(InetAddress address, int port, JsonObject object) {
		if (!object.has("strings"))
			return null;
		ProxyPlatform platform = (ProxyPlatform) Skungee.getPlatform();
		Set<SkungeeServer> servers = platform.getServers().stream()
				.filter(server -> server.getServerData().hasReceiver())
				.collect(Collectors.toSet());
		if (object.has("servers") && object.get("servers").getAsJsonArray().size() != 0) {
			List<String> defined = Streams.stream(object.get("servers").getAsJsonArray())
					.map(element -> element.getAsString())
					.collect(Collectors.toList());
			servers.removeIf(server -> !defined.stream().anyMatch(string -> string.equalsIgnoreCase(server.getName())));
		}
		JsonObject returning = new JsonObject();
		JsonArray array = new JsonArray();
		object.get("strings").getAsJsonArray().forEach(element -> array.add(element.getAsString()));
		returning.add("strings", array);
		JapsonServer japson = platform.getJapsonServer();
		for (SkungeeServer server : servers) {
			ServerData serverData = server.getServerData();
			japson.sendPacket(serverData.getJapsonAddress().getAddress(), serverData.getReceiverPort(), new Packet(Packets.SIGNAL.getPacketId()) {
				@Override
				public JsonObject toJson() {
					return returning;
				}
			});
		}
		return null;
	}

}
