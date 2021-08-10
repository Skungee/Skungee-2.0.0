package com.skungee.proxy.handlers;

import java.net.InetSocketAddress;

import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Handler;
import com.sitrica.japson.shared.Packet;
import com.skungee.proxy.ProxyPlatform;
import com.skungee.proxy.ProxySkungee;
import com.skungee.shared.Packets;
import com.skungee.shared.objects.SkungeeServer;

public class ServerCommandHandler extends Handler {

	public ServerCommandHandler() {
		super(Packets.SERVER_COMMAND.getPacketId());
	}

	@Override
	public JsonObject handle(InetSocketAddress address, JsonObject object) {
		if (!object.has("commands") || !object.has("servers"))
			return null;
		ProxyPlatform platform = ProxySkungee.getPlatform();
		object.get("servers").getAsJsonArray().forEach(element -> {
			try {
				SkungeeServer server = platform.getServer(element.getAsString()).orElseThrow();
				platform.getJapsonServer().sendPacket(server.getServerData().getJapsonAddress(), new Packet(Packets.SERVER_COMMAND.getPacketId()) {
					@Override
					public JsonObject toJson() {
						object.remove("servers");
						return object;
					}
				});
			} catch (Exception e) {
				return;
			}
		});
		return null;
	}

}
