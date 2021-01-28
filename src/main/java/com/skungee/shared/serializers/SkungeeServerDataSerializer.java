package com.skungee.shared.serializers;

import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;
import com.sitrica.japson.gson.JsonArray;
import com.sitrica.japson.gson.JsonDeserializationContext;
import com.sitrica.japson.gson.JsonElement;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.gson.JsonParseException;
import com.sitrica.japson.gson.JsonSerializationContext;
import com.sitrica.japson.shared.Serializer;
import com.skungee.proxy.ServerDataManager.ServerData;

public class SkungeeServerDataSerializer implements Serializer<ServerData> {

	@Override
	public ServerData deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		if (!object.has("motd"))
			throw new JsonParseException("A ServerData json element did not contain the property 'motd'");
		if (!object.has("limit"))
			throw new JsonParseException("A ServerData json element did not contain the property 'limit'");
		if (!object.has("version"))
			throw new JsonParseException("A ServerData json element did not contain the property 'version'");
		if (!object.has("whitelisted"))
			throw new JsonParseException("A ServerData json element did not contain the property 'whitelisted'");
		if (!object.has("japson-address"))
			throw new JsonParseException("A ServerData json element did not contain the property 'japson-address'");
		if (!object.has("japson-port"))
			throw new JsonParseException("A ServerData json element did not contain the property 'japson-port'");
		InetSocketAddress serverAddress = new InetSocketAddress(object.get("address").getAsString(), object.get("port").getAsInt());
		InetSocketAddress japsonAddress = new InetSocketAddress(object.get("japson-address").getAsString(), object.get("japson-port").getAsInt());
		ServerData data = new ServerData(serverAddress, japsonAddress);
		JsonElement limitElement = object.get("limit");
		if (limitElement != null && !limitElement.isJsonNull())
			data.setMaxPlayerLimit(limitElement.getAsInt());
		JsonElement versionElement = object.get("version");
		if (versionElement != null && !versionElement.isJsonNull())
			data.setVersion(versionElement.getAsString());
		JsonElement motdElement = object.get("motd");
		if (motdElement != null && !motdElement.isJsonNull())
			data.setMotd(motdElement.getAsString());
		data.setWhitelisted(Streams.stream(object.get("whitelisted").getAsJsonArray())
				.map(element -> element.getAsString())
				.map(string -> UUID.fromString(string))
				.filter(uuid -> uuid != null)
				.collect(Collectors.toSet()));
		if (object.has("receiver-port"))
			data.setReceiverPort(object.get("receiver-port").getAsInt());

		// Scripts
		JsonArray scripts = object.get("scripts").getAsJsonArray();
		scripts.forEach(element -> {
			JsonObject script = element.getAsJsonObject();
			if (!script.has("name") || !script.has("lines"))
				return;
			String name = script.get("name").getAsString();
			String lines = script.get("lines").getAsString();
			data.addScript(name, lines);
		});
		return data;
	}

	@Override
	public JsonElement serialize(ServerData data, Type type, JsonSerializationContext context) {
		JsonObject object = new JsonObject();
		object.addProperty("limit", data.getMaxPlayerLimit());
		object.addProperty("version", data.getVersion());
		object.addProperty("motd", data.getMotd());
		object.addProperty("address", data.getAddress().getHostName());
		object.addProperty("port", data.getAddress().getPort());
		JsonArray whitelisted = new JsonArray();
		data.getWhitelisted().forEach(uuid -> whitelisted.add(uuid + ""));
		object.add("whitelisted", whitelisted);
		object.addProperty("japson-address", data.getJapsonAddress().getHostName());
		object.addProperty("japson-port", data.getJapsonAddress().getPort());
		if (data.hasReceiver())
			object.addProperty("receiver-port", data.getReceiverPort());
		JsonArray array = new JsonArray();
		for (String key : data.getScripts().keySet()) {
			JsonObject script = new JsonObject();
			script.addProperty("name", key);
			script.addProperty("lines", data.getScripts().get(key));
			array.add(script);
		}
		object.add("scripts", array);
		return object;
	}

}
