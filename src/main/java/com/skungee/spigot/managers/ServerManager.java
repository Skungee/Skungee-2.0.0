package com.skungee.spigot.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sitrica.japson.gson.JsonArray;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.ReturnablePacket;
import com.skungee.shared.Packets;
import com.skungee.shared.objects.SkungeeServer;
import com.skungee.shared.serializers.SkungeeServerSerializer;
import com.skungee.spigot.SpigotSkungee;

public class ServerManager {

	private static final SkungeeServerSerializer serializer = new SkungeeServerSerializer();
	private static final LoadingCache<String, Optional<SkungeeServer>> cache = CacheBuilder.newBuilder()
			.expireAfterAccess(5, TimeUnit.MINUTES)
			.maximumSize(100)
			.build(new CacheLoader<String, Optional<SkungeeServer>>() {
					public Optional<SkungeeServer> load(String name) {
						if (name == null)
							return Optional.empty();
						try {
							SkungeeServer server = SpigotSkungee.getInstance().getJapsonClient().sendPacket(new ReturnablePacket<SkungeeServer>(Packets.SERVERS.getPacketId()) {

								@Override
								public JsonObject toJson() {
									JsonObject object = new JsonObject();
									object.addProperty("name", name);
									return object;
								}

								@Override
								public SkungeeServer getObject(JsonObject object) {
									if (!object.has("server"))
										return null;
									return serializer.deserialize(object.get("server"), SkungeeServer.class, null);
								}

							});
							if (server == null)
								return Optional.empty();
							return Optional.of(server);
						} catch (TimeoutException | InterruptedException | ExecutionException e) {
							return null;
						}
					}
			});

	public static Optional<SkungeeServer> getServer(String name) {
		try {
			return cache.get(name);
		} catch (ExecutionException e) {
			return Optional.empty();
		}
	}

	/**
	 * This method will do a hard lookup and refresh the cache.
	 * 
	 * @return
	 */
	public static List<SkungeeServer> getServers() {
		try {
			List<SkungeeServer> servers = SpigotSkungee.getInstance().getJapsonClient().sendPacket(new ReturnablePacket<List<SkungeeServer>>(Packets.SERVERS.getPacketId()) {

				@Override
				public JsonObject toJson() {
					return new JsonObject();
				}

				@Override
				public List<SkungeeServer> getObject(JsonObject object) {
					if (!object.has("servers"))
						return null;
					List<SkungeeServer> servers = new ArrayList<>();
					JsonArray array = object.get("servers").getAsJsonArray();
					array.forEach(element -> serializer.deserialize(element, SkungeeServer.class, null));
					return servers;
				}
			});
			servers.forEach(server -> cache.put(server.getName(), Optional.of(server)));
			return servers;
		} catch (TimeoutException | InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

}
