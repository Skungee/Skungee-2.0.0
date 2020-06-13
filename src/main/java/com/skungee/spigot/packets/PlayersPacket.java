package com.skungee.spigot.packets;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.jdt.annotation.Nullable;

import com.sitrica.japson.gson.JsonArray;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.ReturnablePacket;
import com.skungee.shared.Packets;
import com.skungee.shared.objects.SkungeePlayer;
import com.skungee.shared.objects.SkungeeServer;
import com.skungee.shared.serializers.SkungeePlayerSerializer;

public class PlayersPacket extends ReturnablePacket<List<SkungeePlayer>> {

	private final SkungeePlayerSerializer serializer = new SkungeePlayerSerializer();
	private SkungeeServer[] servers;
	private String[] names;
	private UUID[] uuids;

	public PlayersPacket(@Nullable SkungeeServer... servers) {
		super(Packets.PLAYERS.getPacketId());
		this.servers = servers;
	}

	public void setServers(SkungeeServer... servers) {
		this.servers = servers;
	}

	public void setNames(String... names) {
		this.names = names;
	}

	public void setUniqueIds(UUID... uuids) {
		this.uuids = uuids;
	}

	@Override
	public JsonObject toJson() {
		JsonObject object = new JsonObject();
		if (servers != null) {
			JsonArray array = new JsonArray();
			for (SkungeeServer server : servers)
				array.add(server.getName());
			object.add("servers", array);
		}
		if (names != null) {
			JsonArray array = new JsonArray();
			for (String name : names)
				array.add(name);
			object.add("names", array);
		}
		if (uuids != null) {
			JsonArray array = new JsonArray();
			for (UUID uuid : uuids)
				array.add(uuid + "");
			object.add("uuids", array);
		}
		return object;
	}

	@Override
	public List<SkungeePlayer> getObject(JsonObject object) {
		List<SkungeePlayer> players = new ArrayList<>();
		if (!object.has("players"))
			return players;
		object.get("players").getAsJsonArray().forEach(element -> {
			try {
				players.add(serializer.deserialize(element, SkungeePlayer.class, null));
			} catch (Exception e) {}
		});
		return players;
	}

}
