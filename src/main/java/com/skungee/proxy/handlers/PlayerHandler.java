package com.skungee.proxy.handlers;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;
import com.sitrica.japson.gson.JsonArray;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Handler;
import com.skungee.proxy.ProxyPlatform;
import com.skungee.proxy.ProxySkungee;
import com.skungee.shared.Packets;
import com.skungee.shared.objects.SkungeePlayer;
import com.skungee.shared.serializers.SkungeePlayerSerializer;

public class PlayerHandler extends Handler {

	private final SkungeePlayerSerializer serializer = new SkungeePlayerSerializer();

	public PlayerHandler() {
		super(Packets.PLAYERS.getPacketId());
	}

	@Override
	public JsonObject handle(InetSocketAddress address, JsonObject object) {
		JsonObject returning = new JsonObject();
		JsonArray array = new JsonArray();
		ProxyPlatform platform = ProxySkungee.getPlatform();
		if (object.has("servers") && object.get("servers").getAsJsonArray().size() != 0) {
			List<String> serverNames = Streams.stream(object.get("servers").getAsJsonArray())
					.map(element -> element.getAsString())
					.collect(Collectors.toList());
			platform.getPlayers().stream()
					.filter(player -> serverNames.contains(player.getCurrentServer()))
					.forEach(player -> array.add(serializer.serialize(player, SkungeePlayer.class, null)));
		} else if (object.has("names")) {
			if (object.get("names").getAsJsonArray().size() == 0)
				return returning;
			List<String> names = Streams.stream(object.get("names").getAsJsonArray())
					.map(element -> element.getAsString())
					.collect(Collectors.toList());
			platform.getPlayers().stream()
					.filter(player -> names.contains(player.getName()))
					.forEach(player -> array.add(serializer.serialize(player, SkungeePlayer.class, null)));
		} else if (object.has("uuids")) {
			if (object.get("uuids").getAsJsonArray().size() == 0)
				return returning;
			List<UUID> names = Streams.stream(object.get("uuids").getAsJsonArray())
					.map(element -> {
						try {
							return UUID.fromString(element.getAsString());
						} catch (Exception e) {
							return null;
						}
					})
					.collect(Collectors.toList());
			platform.getPlayers().stream()
					.filter(player -> names.contains(player.getUniqueId()))
					.forEach(player -> array.add(serializer.serialize(player, SkungeePlayer.class, null)));
		} else {
			platform.getPlayers().forEach(player -> array.add(serializer.serialize(player, SkungeePlayer.class, null)));
		}
		returning.add("players", array);
		return returning;
	}

}
