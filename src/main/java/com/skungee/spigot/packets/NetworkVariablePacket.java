package com.skungee.spigot.packets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import com.sitrica.japson.gson.JsonArray;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.ReturnablePacket;
import com.skungee.shared.Packets;
import com.skungee.shared.objects.NetworkVariable;
import com.skungee.shared.serializers.NetworkVariableSerializer;

/**
 * Only used when returning variables and when using a changer.
 * For getting, just send a string of the VariableName to the handler.
 */
public class NetworkVariablePacket extends ReturnablePacket<List<NetworkVariable>> {

	private final NetworkVariableSerializer serializer = new NetworkVariableSerializer();
	private final NetworkVariable[] variables;
	private String[] names;

	public NetworkVariablePacket(@Nullable NetworkVariable... variables) {
		super(Packets.VARIABLES.getPacketId());
		this.variables = variables;
	}

	public NetworkVariablePacket setNames(String... names) {
		this.names = names;
		return this;
	}

	@Override
	public JsonObject toJson() {
		JsonObject object = new JsonObject();
		if (variables != null && variables.length > 0) {
			JsonArray array = new JsonArray();
			for (NetworkVariable variable : variables)
				array.add(serializer.serialize(variable, NetworkVariable.class, null));
			object.add("variables", array);
		}
		if (names != null && names.length > 0) {
			JsonArray array = new JsonArray();
			for (String name : names)
				array.add(name);
			object.add("names", array);
		}
		return object;
	}

	@Override
	public List<NetworkVariable> getObject(JsonObject object) {
		List<NetworkVariable> variables = new ArrayList<>();
		if (!object.has("variables"))
			return variables;
		object.get("variables").getAsJsonArray().forEach(element -> {
			try {
				variables.add(serializer.deserialize(element, NetworkVariable.class, null));
			} catch (Exception e) {}
		});
		return variables;
	}

}
