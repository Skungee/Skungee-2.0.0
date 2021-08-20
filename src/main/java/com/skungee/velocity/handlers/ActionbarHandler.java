package com.skungee.velocity.handlers;

import java.net.InetSocketAddress;
import java.util.UUID;

import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Executor;
import com.skungee.shared.Packets;
import com.velocitypowered.api.proxy.ProxyServer;

import net.kyori.adventure.text.Component;

public class ActionbarHandler extends Executor {

	private final ProxyServer proxy;

	public ActionbarHandler(ProxyServer proxy) {
		super(Packets.ACTIONBAR.getPacketId());
		this.proxy = proxy;
	}

	@Override
	public void execute(InetSocketAddress address, JsonObject object) {
		if (!object.has("message") || !object.has("players"))
			return;
		String message = object.get("message").getAsString();
		object.get("players").getAsJsonArray().forEach(element -> {
			try {
				UUID uuid = UUID.fromString(element.getAsString());
				proxy.getPlayer(uuid).ifPresent(player -> player.sendActionBar(Component.text(message)));
			} catch (Exception e) {
				return;
			}
		});
	}

}
