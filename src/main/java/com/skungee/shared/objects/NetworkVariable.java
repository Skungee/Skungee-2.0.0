package com.skungee.shared.objects;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;

public class NetworkVariable {

	private SkriptChangeMode changer;
	private Value[] values;
	private final String name;

	public NetworkVariable(String name, Value... values) {
		this.values = values;
		this.name = name;
	}

	public void setChanger(SkriptChangeMode changer) {
		this.changer = changer;
	}

	public Optional<SkriptChangeMode> getChanger() {
		return Optional.ofNullable(changer);
	}

	public String getVariableString() {
		return name;
	}

	public Value[] getValues() {
		return values;
	}

	public String toString() {
		return "name=" + name + ", " + Arrays.toString(values);
	}

	/**
	 * A replica of Skript's Value for Skript variables.
	 * Transfers to Skript's Value later on the Spigot side.
	 */
	public final static class Value implements Serializable {

		private static final long serialVersionUID = 1428760897685648784L;
		public String type;
		public byte[] data;

		public Value(String type, byte[] data) {
			this.type = type;
			this.data = data;
		}

		public String toString() {
			return "type=" + type + ", data=" + Arrays.toString(data);
		}

		public boolean isSimilar(Value compare) {
			return compare.type.equals(type) && Arrays.equals(compare.data, data);
		}

	}

	public enum SkriptChangeMode {
		ADD,
		SET,
		REMOVE,
		REMOVE_ALL,
		DELETE,
		RESET;
	}

}
