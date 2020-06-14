package com.skungee.velocity.handlers;

import java.net.InetAddress;
import java.util.Optional;
import java.util.UUID;

import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Executor;
import com.skungee.shared.Packets;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

public class ConnectHandler extends Executor {

	private final ProxyServer proxy;

	public ConnectHandler(ProxyServer proxy) {
		super(Packets.CONNECT.getPacketId());
		this.proxy = proxy;
	}

	@Override
	public void execute(InetAddress address, int port, JsonObject object) {
		if (!object.has("server") || !object.has("players"))
			return;
		Optional<RegisteredServer> optional = proxy.getServer(object.get("server").getAsString());
		if (!optional.isPresent())
			return;
		RegisteredServer server = optional.get();
		object.get("players").getAsJsonArray().forEach(element -> {
			try {
				UUID uuid = UUID.fromString(element.getAsString());
				proxy.getPlayer(uuid).ifPresent(player -> player.createConnectionRequest(server).fireAndForget());
			} catch (Exception e) {
				return;
			}
		});
	}

}
