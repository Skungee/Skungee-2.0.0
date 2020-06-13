package com.skungee.shared.serializers;

import java.lang.reflect.Type;
import java.util.UUID;

import com.sitrica.japson.gson.JsonDeserializationContext;
import com.sitrica.japson.gson.JsonElement;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.gson.JsonParseException;
import com.sitrica.japson.gson.JsonSerializationContext;
import com.sitrica.japson.shared.Serializer;
import com.skungee.shared.objects.SkungeePlayer;

public class SkungeePlayerSerializer implements Serializer<SkungeePlayer> {

	@Override
	public SkungeePlayer deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = element.getAsJsonObject();
		if (!object.has("name"))
			throw new JsonParseException("A SkungeeServer json element did not contain the property 'name'");
		if (!object.has("uuid"))
			throw new JsonParseException("A SkungeeServer json element did not contain the property 'uuid'");
		return new SkungeePlayer(object.get("name").getAsString(), UUID.fromString(object.get("uuid").getAsString()));
	}

	@Override
	public JsonElement serialize(SkungeePlayer player, Type type, JsonSerializationContext context) {
		JsonObject object = new JsonObject();
		object.addProperty("uuid", player.getUniqueId() + "");
		object.addProperty("name", player.getName());
		return object;
	}

}
