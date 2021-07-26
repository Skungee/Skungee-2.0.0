package com.skungee.proxy.handlers;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Executor;
import com.skungee.shared.Packets;

public class CancellationHandler extends Executor {

	private final static Map<String, InetSocketAddress> cancellations = new HashMap<>();

	public CancellationHandler() {
		super(Packets.CANCELLATION.getPacketId());
	}

	public final static boolean isCancelled(String event, InetSocketAddress address) {
		InetSocketAddress existing = cancellations.get(event);
		if (existing == null)
			return false;
		return existing.equals(address);
	}

	public final static void clear(InetSocketAddress address) {
		cancellations.entrySet().removeIf(entry -> entry.getValue().equals(address));
	}

	@Override
	public void execute(InetSocketAddress address, JsonObject object) {
		if (!object.has("event") || !object.has("cancel") || !object.has("address") || !object.has("port"))
			return;
		InetSocketAddress serverAddress = new InetSocketAddress(object.get("address").getAsString(), object.get("port").getAsInt());
		String event = object.get("event").getAsString();
		boolean cancel = object.get("cancel").getAsBoolean();
		if (!cancel) {
			cancellations.remove(event, serverAddress);
			return;
		}
		cancellations.put(event, serverAddress);
	}

}
