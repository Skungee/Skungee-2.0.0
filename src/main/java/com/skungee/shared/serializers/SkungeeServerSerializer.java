package com.skungee.shared.serializers;

import java.lang.reflect.Type;

import com.sitrica.japson.gson.JsonDeserializationContext;
import com.sitrica.japson.gson.JsonElement;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.gson.JsonParseException;
import com.sitrica.japson.gson.JsonSerializationContext;
import com.sitrica.japson.shared.Serializer;
import com.skungee.shared.objects.SkungeeServer;

public class SkungeeServerSerializer implements Serializer<SkungeeServer> {

	@Override
	public SkungeeServer deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = element.getAsJsonObject();
		if (!object.has("name"))
			throw new JsonParseException("A SkungeeServer json element did not contain the property 'name'");
		if (!object.has("online"))
			throw new JsonParseException("A SkungeeServer json element did not contain the property 'online'");
		return new SkungeeServer(object.get("name").getAsString(), object.get("online").getAsBoolean());
	}

	@Override
	public JsonElement serialize(SkungeeServer server, Type type, JsonSerializationContext context) {
		JsonObject object = new JsonObject();
		object.addProperty("online", server.isOnline());
		object.addProperty("name", server.getName());
		return object;
	}

}
