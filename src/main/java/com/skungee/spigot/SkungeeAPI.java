package com.skungee.spigot;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import com.sitrica.japson.client.JapsonClient;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Packet;
import com.sitrica.japson.shared.ReturnablePacket;
import com.skungee.shared.Packets;
import com.skungee.shared.objects.SkungeePlayer;
import com.skungee.shared.objects.SkungeeServer;
import com.skungee.spigot.managers.ServerManager;
import com.skungee.spigot.packets.PlayersPacket;

public class SkungeeAPI {

	private final JapsonClient japson;

	public SkungeeAPI(SpigotSkungee instance) {
		this.japson = instance.getJapsonClient();
	}

	/**
	 * Grab SkungeePlayers from UUIDs.
	 * 
	 * @param uuids The UUID's to be converted to SkungeePlayers.
	 * @return The SkungeePlayers converted.
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws TimeoutException 
	 */
	public List<SkungeePlayer> getPlayers(UUID... uuids) throws TimeoutException, InterruptedException, ExecutionException {
		PlayersPacket packet = new PlayersPacket();
		packet.setUniqueIds(uuids);
		return japson.sendPacket(packet);
	}

	/**
	 * Grab SkungeePlayers from String usernames.
	 * 
	 * @param usernames The String's to be converted to SkungeePlayers.
	 * @return The SkungeePlayers converted.
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws TimeoutException 
	 */
	public List<SkungeePlayer> getPlayers(String... usernames) throws TimeoutException, InterruptedException, ExecutionException {
		PlayersPacket packet = new PlayersPacket();
		packet.setNames(usernames);
		return japson.sendPacket(packet);
	}

	/**
	 * Grab SkungeePlayers from UUIDs on a defined server.
	 * 
	 * @param server The SkungeeServer to grab players from.
	 * @param uuids The UUID's to be converted to SkungeePlayers.
	 * @return The SkungeePlayers converted.
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws TimeoutException 
	 */
	public List<SkungeePlayer> getPlayers(SkungeeServer server, UUID... uuids) throws TimeoutException, InterruptedException, ExecutionException {
		PlayersPacket packet = new PlayersPacket();
		packet.setUniqueIds(uuids);
		packet.setServers(server);
		return japson.sendPacket(packet);
	}

	/**
	 * Grab SkungeePlayers from String usernames on a defined server.
	 * 
	 * @param server The SkungeeServer to grab players from.
	 * @param usernames The String's to be converted to SkungeePlayers.
	 * @return The SkungeePlayers converted.
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws TimeoutException 
	 */
	public List<SkungeePlayer> getPlayers(SkungeeServer server, String... usernames) throws TimeoutException, InterruptedException, ExecutionException {
		PlayersPacket packet = new PlayersPacket();
		packet.setNames(usernames);
		packet.setServers(server);
		return japson.sendPacket(packet);
	}

	/**
	 * Grab SkungeePlayers from a defined server.
	 * 
	 * @param servers The SkungeeServer's to grab players from.
	 * @return The SkungeePlayers converted.
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws TimeoutException 
	 */
	public List<SkungeePlayer> getPlayers(SkungeeServer... servers) throws TimeoutException, InterruptedException, ExecutionException {
		PlayersPacket packet = new PlayersPacket();
		packet.setServers(servers);
		return japson.sendPacket(packet);
	}

	/**
	 * Send a custom non-returning packet.
	 * 
	 * @param packet The Japson Packet to send.
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws TimeoutException 
	 */
	public void sendJson(JsonObject json) throws InterruptedException, ExecutionException, TimeoutException {
		Packet packet = new Packet(Packets.API.getPacketId()) {
			@Override
			public JsonObject toJson() {
				return json;
			}
		};
		japson.sendPacket(packet);
	}

	/**
	 * Send a custom returning packet.
	 * 
	 * @param packet The Japson ReturningPacket to send.
	 * @return The data returned from the handler on the proxy.
	 */
	public <T> T sendPacket(JsonObject json, Function<JsonObject, T> function) throws TimeoutException, InterruptedException, ExecutionException {
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
		return japson.sendPacket(packet);
	}

	/**
	 * Will grab a SkungeeServer instance if the servers are indeed connected and found.
	 * 
	 * @param servers The input to search for.
	 * @return The SkungeeServers of which were found by the input strings.
	 */
	public List<SkungeeServer> getServers(String... servers) {
		return ServerManager.getServers(servers);
	}

	/**
	 * Grabs all the connected servers on from the Skungee Proxy side.
	 * 
	 * @return connected servers on from the Skungee Proxy side.
	 */
	public static List<SkungeeServer> getServers() {
		return ServerManager.getServers();
	}

}
