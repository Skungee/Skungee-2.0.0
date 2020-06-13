package com.skungee.shared;

public class Skungee {

	private static Platform platform;

	public static void setPlatform(Platform platform) throws IllegalAccessException {
		if (Skungee.platform != null)
			throw new IllegalAccessException("The platform has already been set.");
		Skungee.platform = platform;
	}

	public static Platform getPlatform() {
		return platform;
	}

}
