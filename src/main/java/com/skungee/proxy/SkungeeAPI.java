package com.skungee.proxy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Handler;
import com.sitrica.japson.shared.Packet;
import com.sitrica.japson.shared.ReturnablePacket;
import com.skungee.shared.Packets;
import com.skungee.shared.objects.SkungeePlayer;
import com.skungee.shared.objects.SkungeeServer;

public class SkungeeAPI {

	private static ProxyPlatform platform;

	public static void register(ProxyPlatform platform) {
		if (SkungeeAPI.platform != null)
			return;
		SkungeeAPI.platform = platform;
	}

	/**
	 * Grab SkungeePlayers from UUIDs.
	 * 
	 * @param uuids The UUID's to be converted to SkungeePlayers.
	 * @return The SkungeePlayers converted.
	 */
	public static List<SkungeePlayer> getPlayers(UUID... uuids) {
		return Arrays.stream(uuids)
				.map(uuid -> platform.getPlayer(uuid))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
	}

	/**
	 * Grab SkungeePlayers from String usernames.
	 * 
	 * @param usernames The String's to be converted to SkungeePlayers.
	 * @return The SkungeePlayers converted.
	 */
	public static List<SkungeePlayer> getPlayers(String... usernames) {
		return Arrays.stream(usernames)
				.map(username -> platform.getPlayer(username))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
	}

	/**
	 * Grabs all the connected servers.
	 * 
	 * @return connected servers.
	 */
	public static Set<SkungeeServer> getServers() {
		return Collections.unmodifiableSet(platform.getServers());
	}

	/**
	 * Send a custom non-returning packet.
	 * 
	 * @param packet The Japson Packet to send.
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws TimeoutException 
	 */
	public static void sendJson(SkungeeServer server, JsonObject json) throws InterruptedException, ExecutionException, TimeoutException {
		Packet packet = new Packet(Packets.API.getPacketId()) {
			@Override
			public JsonObject toJson() {
				return json;
			}
		};
		platform.getJapsonServer().sendPacket(server.getServerData().getJapsonAddress(), packet);
	}

	/**
	 * Send a custom returning packet.
	 * 
	 * @param packet The Japson ReturningPacket to send.
	 * @return The data returned from the handler on the proxy.
	 */
	public static <T> T sendPacket(SkungeeServer server, JsonObject json, Function<JsonObject, T> function) throws TimeoutException, InterruptedException, ExecutionException {
		ReturnablePacket<T> packet = new ReturnablePacket<T>(Packets.API.getPacketId()) {
			@Override
			public JsonObject toJson() {
				return json;
			}
			@Override
			public T getObject(JsonObject object) {
				return function.apply(object);
			}
		};
		return platform.getJapsonServer().sendPacket(server.getServerData().getJapsonAddress(), packet);
	}

	public static <H extends Handler> void registerHandler(@SuppressWarnings("unchecked") H... handlers) {
		platform.getJapsonServer().registerHandlers(handlers);
	}

}
