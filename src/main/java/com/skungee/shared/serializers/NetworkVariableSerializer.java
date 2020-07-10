package com.skungee.shared.serializers;

import java.lang.reflect.Type;

import com.google.common.collect.Streams;
import com.sitrica.japson.gson.JsonArray;
import com.sitrica.japson.gson.JsonDeserializationContext;
import com.sitrica.japson.gson.JsonElement;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.gson.JsonParseException;
import com.sitrica.japson.gson.JsonSerializationContext;
import com.sitrica.japson.shared.Serializer;
import com.skungee.shared.objects.NetworkVariable;
import com.skungee.shared.objects.NetworkVariable.SkriptChangeMode;
import com.skungee.shared.objects.NetworkVariable.Value;

public class NetworkVariableSerializer implements Serializer<NetworkVariable> {

	@Override
	public NetworkVariable deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = element.getAsJsonObject();
		if (!object.has("name"))
			throw new JsonParseException("A NetworkVariable json element did not contain the property 'name'");
		if (!object.has("values"))
			throw new JsonParseException("A NetworkVariable json element did not contain the property 'values'");
		Value[] values = Streams.stream(object.get("values").getAsJsonArray())
			.map(value -> {
				JsonObject valueObject = value.getAsJsonObject();
				String typeName = valueObject.get("type").getAsString();
				JsonArray bytes = valueObject.get("bytes").getAsJsonArray();
				byte[] data = new byte[bytes.size()];
				int i = 0;
				for (JsonElement byteElement : bytes) {
					data[i] = byteElement.getAsByte();
					i++;
				}
				return new Value(typeName, data);
			})
			.toArray(Value[]::new);
		NetworkVariable variable = new NetworkVariable(object.get("name").getAsString(), values);
		if (object.has("changer"))
			variable.setChanger(SkriptChangeMode.valueOf(object.get("changer").getAsString()));
		return variable;
	}

	@Override
	public JsonElement serialize(NetworkVariable variable, Type type, JsonSerializationContext context) {
		JsonObject object = new JsonObject();
		object.addProperty("name", variable.getVariableString());
		variable.getChanger().ifPresent(changer -> object.addProperty("changer", changer.name()));
		JsonArray values = new JsonArray();
		for (Value value : variable.getValues()) {
			JsonObject valueObject = new JsonObject();
			valueObject.addProperty("type", value.type);
			JsonArray bytes = new JsonArray();
			for (byte data : value.data)
				bytes.add(data);
			valueObject.add("bytes", bytes);
			values.add(valueObject);
		}
		object.add("values", values);
		return object;
	}

}
