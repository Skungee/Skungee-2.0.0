package com.skungee.proxy.handlers;

import java.net.InetSocketAddress;

import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Executor;
import com.skungee.proxy.ProxySkungee;
import com.skungee.proxy.managers.EventManager;
import com.skungee.shared.Packets;

public class CancellationHandler extends Executor {

	public CancellationHandler() {
		super(Packets.CANCELLATION.getPacketId());
	}

	@Override
	public void execute(InetSocketAddress address, JsonObject object) {
		if (!object.has("event") || !object.has("cancel") || !object.has("address") || !object.has("port"))
			return;
		InetSocketAddress serverAddress = new InetSocketAddress(object.get("address").getAsString(), object.get("port").getAsInt());
		String event = object.get("event").getAsString();
		boolean cancel = object.get("cancel").getAsBoolean();
		EventManager eventManager = ProxySkungee.getPlatform().getEventManager();
		if (!cancel) {
			eventManager.setCancelled(false, event, serverAddress);
			return;
		}
		eventManager.setCancelled(true, event, serverAddress);
	}

}
