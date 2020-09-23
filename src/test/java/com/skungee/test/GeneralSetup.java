package com.skungee.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import com.sitrica.japson.client.JapsonClient;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.server.JapsonServer;
import com.sitrica.japson.shared.Handler;
import com.sitrica.japson.shared.ReturnablePacket;
import com.skungee.shared.Packets;
import com.skungee.spigot.utils.Utils;

public class GeneralSetup {

	@Order(1)
	@Test
	public void start() throws UnknownHostException, SocketException, TimeoutException, InterruptedException, ExecutionException, NoSuchAlgorithmException {
		assertTrue(!Utils.isPortTaken(1337));
		JapsonClient client = new JapsonClient(1337);
		client.makeSureConnectionValid();
		MessageDigest sha256 = MessageDigest.getInstance("SHA-256");        
	    byte[] passwordBytes = "test".getBytes();
	    byte[] passwordHash = sha256.digest(passwordBytes);
		client.setPassword(new String(passwordHash));
		client.enableDebug();
		JapsonServer server = new JapsonServer(1337);
		server.setPassword(new String(passwordHash));
		server.enableDebug();
		server.registerHandlers(new Reflections("com.skungee.proxy.handlers", "com.skungee.bungeecord.handlers")
				.getSubTypesOf(Handler.class).stream().map(clazz -> {
					try {
						return clazz.newInstance();
					} catch (InstantiationException | IllegalAccessException e) {
						e.printStackTrace();
						return null;
					}
				}).filter(handler -> handler != null).toArray(Handler[]::new));
		server.registerHandlers(new Handler(Packets.API.getPacketId()) {

			@Override
			public JsonObject handle(InetAddress address, int port, JsonObject json) {
				JsonObject returning = new JsonObject();
				assertNotNull(json);
				assertTrue(json.has("test"));
				String got = json.get("test").getAsString();
				assertEquals(got, "Hello World!");
				returning.addProperty("return", "Returning!");
				return returning;
			}

		});
		String returned = client.sendPacket(new ReturnablePacket<String>(Packets.API.getPacketId()) {

			@Override
			public String getObject(JsonObject json) {
				return json.get("return").getAsString();
			}

			@Override
			public JsonObject toJson() {
				JsonObject object = new JsonObject();
				object.addProperty("test", "Hello World!");
				return object;
			}

		});
		assertEquals(returned, "Returning!");
		assertTrue(Utils.isPortTaken(server.getPort()));
		server.shutdown();
	}

}
