package com.skungee.bungeecord.handlers;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Handler;
import com.skungee.shared.Packets;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

public class MessageHandler extends Handler {

	public MessageHandler() {
		super(Packets.MESSAGE.getPacketId());
	}

	@Override
	public JsonObject handle(InetSocketAddress address, JsonObject object) {
		if (!object.has("strings") || !object.has("players"))
			return null;
		ProxyServer proxy = ProxyServer.getInstance();
		Set<String> messages = Streams.stream(object.get("strings").getAsJsonArray())
				.map(element -> element.getAsString())
				.collect(Collectors.toSet());
		object.get("players").getAsJsonArray().forEach(element -> {
			try {
				UUID uuid = UUID.fromString(element.getAsString());
				Optional.ofNullable(proxy.getPlayer(uuid))
						.ifPresent(player -> messages.forEach(message -> player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', message)))));
			} catch (Exception e) {
				return;
			}
		});
		return null;
	}

}
