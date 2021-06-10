package com.skungee.proxy;

public class ProxySkungee {

	private static ProxyPlatform platform;

	public static void setPlatform(ProxyPlatform platform) throws IllegalAccessException {
		if (ProxySkungee.platform != null)
			throw new IllegalAccessException("The platform has already been set.");
		ProxySkungee.platform = platform;
	}

	public static ProxyPlatform getPlatform() {
		return platform;
	}

}
