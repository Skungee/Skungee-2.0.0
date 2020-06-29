package com.skungee.spigot.tasks;

import org.bukkit.Bukkit;

import com.sitrica.japson.client.JapsonClient;
import com.sitrica.japson.gson.JsonArray;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Packet;
import com.skungee.shared.Packets;

public class ServerDataTask implements Runnable {

	private final JapsonClient japson;

	public ServerDataTask(JapsonClient japson) {
		this.japson = japson;
	}

	@Override
	public void run() {
		japson.sendPacket(new Packet(Packets.SERVER_DATA.getPacketId()) {
			@Override
			public JsonObject toJson() {
				JsonObject object = new JsonObject();
				object.addProperty("limit", Bukkit.getMaxPlayers());
				object.addProperty("version", Bukkit.getVersion());
				object.addProperty("motd", Bukkit.getMotd());
				object.addProperty("port", Bukkit.getPort());
				JsonArray whitelisted = new JsonArray();
				Bukkit.getWhitelistedPlayers().forEach(player -> whitelisted.add(player.getUniqueId() + ""));
				object.add("whitelisted", whitelisted);
				JsonArray banned = new JsonArray();
				Bukkit.getBannedPlayers().forEach(player -> banned.add(player.getUniqueId() + ""));
				object.add("banned", banned);
				JsonArray operators = new JsonArray();
				Bukkit.getOperators().forEach(player -> operators.add(player.getUniqueId() + ""));
				object.add("operators", operators);
				return object;
			}
		});
	}

}
