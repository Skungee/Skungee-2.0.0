package com.skungee.bungeecord.handlers;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Handler;
import com.skungee.shared.Packets;

import net.md_5.bungee.api.ProxyServer;

public class PlayerCommandHandler extends Handler {

	public PlayerCommandHandler() {
		super(Packets.PROXY_PLAYER_COMMAND.getPacketId());
	}

	@Override
	public JsonObject handle(InetAddress address, int port, JsonObject object) {
		if (!object.has("commands") || !object.has("players"))
			return null;
		ProxyServer proxy = ProxyServer.getInstance();
		Set<String> commands = new HashSet<>();
		object.get("commands").getAsJsonArray().forEach(element -> commands.add(element.getAsString()));
		object.get("players").getAsJsonArray().forEach(element -> {
			try {
				UUID uuid = UUID.fromString(element.getAsString());
				Optional.ofNullable(proxy.getPlayer(uuid))
						.ifPresent(player -> commands.forEach(command -> player.chat(command)));
			} catch (Exception e) {
				return;
			}
		});
		return null;
	}

}
