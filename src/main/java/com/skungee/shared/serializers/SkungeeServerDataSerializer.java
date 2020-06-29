package com.skungee.shared.serializers;

import java.lang.reflect.Type;
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
		if (!object.has("verson"))
			throw new JsonParseException("A ServerData json element did not contain the property 'verson'");
		if (!object.has("whitelisted"))
			throw new JsonParseException("A ServerData json element did not contain the property 'whitelisted'");
		ServerData data = new ServerData();
		data.setMaxPlayerLimit(object.get("limit").getAsInt());
		data.setVersion(object.get("version").getAsString());
		data.setMotd(object.get("motd").getAsString());
		data.setWhitelisted(Streams.stream(object.get("whitelisted").getAsJsonArray())
				.map(element -> element.getAsString())
				.map(string -> UUID.fromString(string))
				.filter(uuid -> uuid != null)
				.collect(Collectors.toSet()));
		return data;
	}

	@Override
	public JsonElement serialize(ServerData data, Type type, JsonSerializationContext context) {
		JsonObject object = new JsonObject();
		object.addProperty("limit", data.getMaxPlayerLimit());
		object.addProperty("version", data.getVersion());
		object.addProperty("motd", data.getMotd());
		JsonArray whitelisted = new JsonArray();
		data.getWhitelisted().forEach(uuid -> whitelisted.add(uuid + ""));
		object.add("whitelisted", whitelisted);
		return object;
	}

}
