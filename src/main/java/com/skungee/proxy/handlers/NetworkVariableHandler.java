package com.skungee.proxy.handlers;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.sitrica.japson.gson.JsonArray;
import com.sitrica.japson.gson.JsonElement;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Handler;
import com.skungee.proxy.ProxyPlatform;
import com.skungee.proxy.ProxySkungee;
import com.skungee.proxy.variables.SkungeeStorage;
import com.skungee.shared.Packets;
import com.skungee.shared.objects.NetworkVariable;
import com.skungee.shared.objects.NetworkVariable.SkriptChangeMode;
import com.skungee.shared.objects.NetworkVariable.Value;
import com.skungee.shared.serializers.NetworkVariableSerializer;

public class NetworkVariableHandler extends Handler {

	private final NetworkVariableSerializer serializer = new NetworkVariableSerializer();

	public NetworkVariableHandler() {
		super(Packets.VARIABLES.getPacketId());
	}

	@Override
	public JsonObject handle(InetAddress address, int port, JsonObject object) {
		JsonObject returning = new JsonObject();
		ProxyPlatform platform = ProxySkungee.getPlatform();
		SkungeeStorage storage = platform.getVariableManager().getMainStorage();
		if (object.has("variables")) {
			JsonArray variables = object.get("variables").getAsJsonArray();
			for (JsonElement element : variables) {
				NetworkVariable variable = serializer.deserialize(element, NetworkVariable.class, null);
				String variableString = variable.getVariableString();
				if (variableString == null)
					return returning;
				Value[] values = variable.getValues();
				Optional<SkriptChangeMode> mode = variable.getChanger();
				if (mode.isPresent()) {
					ArrayList<Value> modify = new ArrayList<Value>();
					Value[] data = storage.get(variableString);
					if (data != null)
						modify = Lists.newArrayList(data);
					if (!variable.areValuesValid() && !(mode.get() == SkriptChangeMode.RESET || mode.get() == SkriptChangeMode.DELETE))
						return returning;
					switch (mode.get()) {
						case ADD:
							storage.delete(variableString);
							for (Value value : values)
								modify.add(value);
							storage.set(variableString, modify.toArray(new Value[modify.size()]));
							break;
						case REMOVE_ALL:
						case REMOVE:
							storage.remove(values, variableString);
							break;
						case DELETE:
						case RESET:
							storage.delete(variableString);
							break;
						case SET:
							storage.set(variableString, values);
							break;
					}
				}
			}
		} else if (object.has("names")) {
			JsonArray variables = new JsonArray();
			Streams.stream(object.get("names").getAsJsonArray())
					.map(element -> element.getAsString())
					.map(name -> new NetworkVariable(name, storage.get(name)))
					.filter(variable -> variable.areValuesValid())
					.map(variable -> serializer.serialize(variable, NetworkVariable.class, null))
					.forEach(element -> variables.add(element));
			returning.add("variables", variables);
		}
		return returning;
	}

}
