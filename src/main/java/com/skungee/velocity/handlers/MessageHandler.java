package com.skungee.velocity.handlers;

import java.net.InetAddress;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Handler;
import com.skungee.shared.Packets;
import com.velocitypowered.api.proxy.ProxyServer;

import net.kyori.adventure.text.Component;

public class MessageHandler extends Handler {

	private final ProxyServer proxy;

	public MessageHandler(ProxyServer proxy) {
		super(Packets.MESSAGE.getPacketId());
		this.proxy = proxy;
	}

	@Override
	public JsonObject handle(InetAddress address, int port, JsonObject object) {
		if (!object.has("strings") || !object.has("players"))
			return null;
		Set<String> messages = Streams.stream(object.get("strings").getAsJsonArray())
				.map(element -> element.getAsString())
				.collect(Collectors.toSet());
		object.get("players").getAsJsonArray().forEach(element -> {
			try {
				UUID uuid = UUID.fromString(element.getAsString());
				proxy.getPlayer(uuid).ifPresent(player -> messages.forEach(message -> player.sendMessage(Component.text(message))));
			} catch (Exception e) {
				return;
			}
		});
		return null;
	}

}
