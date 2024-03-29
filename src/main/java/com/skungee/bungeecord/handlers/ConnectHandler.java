package com.skungee.bungeecord.handlers;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;

import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Handler;
import com.skungee.shared.Packets;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerConnectRequest;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent.Reason;

public class ConnectHandler extends Handler {

	public ConnectHandler() {
		super(Packets.CONNECT.getPacketId());
	}

	@Override
	public JsonObject handle(InetSocketAddress address, JsonObject object) {
		if (!object.has("server") || !object.has("players"))
			return null;
		ProxyServer proxy = ProxyServer.getInstance();
		ServerInfo info = proxy.getServerInfo(object.get("server").getAsString());
		ServerConnectRequest connection = ServerConnectRequest.builder()
				.reason(Reason.PLUGIN)
				.target(info)
				.retry(true)
				.build();
		object.get("players").getAsJsonArray().forEach(element -> {
			try {
				UUID uuid = UUID.fromString(element.getAsString());
				Optional.ofNullable(proxy.getPlayer(uuid))
						.ifPresent(player -> player.connect(connection));
			} catch (Exception e) {
				return;
			}
		});
		return null;
	}

}
