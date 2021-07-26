package com.skungee.spigot.packets;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.bukkit.Bukkit;

import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Packet;
import com.skungee.shared.Packets;
import com.skungee.spigot.SpigotSkungee;

/**
 * Only used when returning variables and when using a changer.
 * For getting, just send a string of the VariableName to the handler.
 */
public class CancellationPacket extends Packet {

	private final boolean cancelled;
	private final String event;

	public CancellationPacket(String event, boolean cancelled) {
		super(Packets.CANCELLATION.getPacketId());
		this.cancelled = cancelled;
		this.event = event;
	}

	@Override
	public JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("cancelled", cancelled);
		object.addProperty("event", event);
		if (!Bukkit.getIp().isEmpty())
			object.addProperty("address", Bukkit.getIp());
		else
			object.addProperty("address", SpigotSkungee.getInstance().getPlatformConfiguration().getBindAddress().getHostName());
		object.addProperty("port", Bukkit.getPort());
		return object;
	}

	public void send() throws TimeoutException, InterruptedException, ExecutionException {
		SpigotSkungee.getInstance().getJapsonClient().sendPacket(this);
	}

}
