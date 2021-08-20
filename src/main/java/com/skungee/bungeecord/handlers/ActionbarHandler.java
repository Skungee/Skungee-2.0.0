package com.skungee.bungeecord.handlers;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;

import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Handler;
import com.skungee.shared.Packets;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

public class ActionbarHandler extends Handler {

	public ActionbarHandler() {
		super(Packets.ACTIONBAR.getPacketId());
	}

	@Override
	public JsonObject handle(InetSocketAddress address, JsonObject object) {
		if (!object.has("message") || !object.has("players"))
			return null;
		ProxyServer proxy = ProxyServer.getInstance();
		String message = object.get("message").getAsString();
		object.get("players").getAsJsonArray().forEach(element -> {
			try {
				UUID uuid = UUID.fromString(element.getAsString());
				Optional.ofNullable(proxy.getPlayer(uuid))
						.ifPresent(player -> player.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message)));
			} catch (Exception e) {
				return;
			}
		});
		return null;
	}

}
