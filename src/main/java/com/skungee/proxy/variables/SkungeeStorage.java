package com.skungee.proxy.variables;

import java.io.File;
import java.util.TreeMap;

import com.skungee.proxy.ProxyPlatform;
import com.skungee.shared.objects.NetworkVariable.Value;

public abstract class SkungeeStorage {

	protected final TreeMap<String, Value[]> variables = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	protected final String variablesFolder;
	protected final ProxyPlatform platform;
	private final String[] names;

	public SkungeeStorage(ProxyPlatform platform, String... names) {
		variablesFolder = platform.getPlatformFolder().getAbsolutePath() + File.separator + "variables" + File.separator;
		this.platform = platform;
		this.names = names;
	}

	public int getSize() {
		return variables.size();
	}

	public String[] getNames() {
		return names;
	}

	public abstract void remove(Value[] objects, String... index);

	public abstract void set(String index, Value[] objects);

	public abstract void delete(String... index);

	public abstract Value[] get(String index);

	/**
	 * @returns true if initialization was successful.
	 */
	protected abstract boolean initialize();

	/**
	 * When a backup is called to be processed based on the configuration time.
	 */
	protected abstract void backup();

}
