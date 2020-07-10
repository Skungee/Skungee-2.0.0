package com.skungee.proxy.variables;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.reflections.Reflections;

import com.skungee.proxy.ProxyConfiguration;
import com.skungee.proxy.ProxyPlatform;

public class VariableManager {

	private final Set<SkungeeStorage> storages = new HashSet<>();
	private final ProxyPlatform platform;
	private SkungeeStorage main;

	public VariableManager(ProxyPlatform platform) {
		this.platform = platform;
		ProxyConfiguration configuration = platform.getPlatformConfiguration();
		if (configuration.isBackupsEnabled()) {
			long minutes = configuration.getMinutesBackupInterval();
			boolean messages = configuration.hasBackupConsoleMessages();
			platform.schedule(() -> {
				main.backup();
				if (messages)
					platform.consoleMessage("Variables have been saved!");
			}, minutes, minutes, TimeUnit.MINUTES);
		}
		storages.addAll(new Reflections("com.skungee.proxy.variables.storage").getSubTypesOf(SkungeeStorage.class).stream()
				.map(clazz -> {
					try {
						return clazz.getConstructor(ProxyPlatform.class).newInstance(platform);
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
						e.printStackTrace();
						return null;
					}
				}).filter(storage -> storage != null)
				.collect(Collectors.toSet()));
		boolean initialize = false;
		for (SkungeeStorage storage : storages) {
			for (String name : storage.getNames()) {
				if (configuration.getVariableDatabaseType().equalsIgnoreCase(name)) {
					initialize = storage.initialize();
					main = storage;
				}
			}
		}
		if (!initialize) {
			platform.consoleMessage("Failed to initialize storage type: " + configuration.getVariableDatabaseType());
			return;
		}
	}

	public void registerStorage(SkungeeStorage storage) {
		storages.add(storage);
		platform.debugMessage("Registered storage type: " + storage.getNames()[0]);
	}

	public SkungeeStorage getMainStorage() {
		return main;
	}

}
