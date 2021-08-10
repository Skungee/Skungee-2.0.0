package com.skungee.proxy.managers;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.server.JapsonServer;
import com.sitrica.japson.shared.Packet;
import com.sitrica.japson.shared.ReturnablePacket;
import com.skungee.proxy.ProxyPlatform;
import com.skungee.shared.Packets;
import com.skungee.shared.objects.ServerData;
import com.skungee.shared.objects.SkungeeServer;

public class EventManager {

	private final SetMultimap<String, InetSocketAddress> cancellations = MultimapBuilder.hashKeys().hashSetValues().build();
	private final ProxyPlatform platform;

	public EventManager(ProxyPlatform platform) {
		this.platform = platform;
	}

	public boolean isCancelled(String event, InetSocketAddress address) {
		Set<InetSocketAddress> existing = cancellations.get(event);
		if (existing == null || existing.isEmpty())
			return false;
		return existing.contains(address);
	}

	public void setCancelled(boolean cancelled, String event, InetSocketAddress... addresses) {
		Set<InetSocketAddress> set = Sets.newHashSet(addresses);
		if (cancelled == false) {
			cancellations.get(event).removeAll(set);
			return;
		}
		cancellations.putAll(event, set);
	}

	public boolean callEvent(String event, JsonObject object, boolean cancellable) {
		if (!object.has("event"))
			object.addProperty("event", event);
		JapsonServer japson = platform.getJapsonServer();
		return platform.getServers().parallelStream()
				.map(SkungeeServer::getServerData)
				.filter(ServerData::hasReceiver)
				.map(data -> {
					try {
						if (!cancellations.get(event).contains(data.getAddress()) || !cancellable) {
							japson.sendPacket(data.getJapsonAddress(), new Packet(Packets.EVENT.getPacketId()) {
								@Override
								public JsonObject toJson() {
									return object;
								}
							});
							return false;
						}
						return japson.sendPacket(data.getJapsonAddress(), new ReturnablePacket<Boolean>(Packets.EVENT.getPacketId()) {

							@Override
							public Boolean getObject(JsonObject cancel) {
								if (!cancel.has("cancel"))
									return false;
								return cancel.get("cancel").getAsBoolean();
							}

							@Override
							public JsonObject toJson() {
								return object;
							}

						});
					} catch (TimeoutException | InterruptedException | ExecutionException exception) {
						platform.debugMessages(exception, "Failed to send cancellation check to Spigot servers. Event: " + event);
						return false;
					}
				}).anyMatch(value -> value == true);
	}

}
