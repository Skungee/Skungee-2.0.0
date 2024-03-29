package com.skungee.proxy.handlers;

import java.net.InetSocketAddress;

import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Executor;
import com.skungee.proxy.ProxyPlatform;
import com.skungee.proxy.ProxySkungee;
import com.skungee.shared.Packets;
import com.skungee.shared.objects.ServerData;
import com.skungee.shared.serializers.SkungeeServerDataSerializer;

public class ServerDataHandler extends Executor {

	private final SkungeeServerDataSerializer serializer;
	private final ProxyPlatform proxy;

	public ServerDataHandler() {
		super(Packets.SERVER_DATA.getPacketId());
		proxy = ProxySkungee.getPlatform();
		serializer = new SkungeeServerDataSerializer();
	}

	@Override
	public void execute(InetSocketAddress address, JsonObject object) {
		InetSocketAddress serverAddress = new InetSocketAddress(object.get("address").getAsString(), object.get("port").getAsInt());
		ServerData data = serializer.deserialize(object, ServerData.class, null);
		proxy.getServerDataManager().set(serverAddress, data);
	}

}
