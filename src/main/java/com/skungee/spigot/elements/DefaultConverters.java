package com.skungee.spigot.elements;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.eclipse.jdt.annotation.Nullable;

import com.skungee.shared.objects.SkungeePlayer;
import com.skungee.shared.objects.SkungeeServer;
import com.skungee.spigot.objects.SkungeePlayerMapper;
import com.skungee.spigot.objects.SkungeeServerMapper;

import ch.njol.skript.classes.Converter;
import ch.njol.skript.registrations.Converters;

public class DefaultConverters {

	static {

		// SkungeeServer
		Converters.registerConverter(SkungeeServer.class, String.class, new Converter<SkungeeServer, String>() {
			@Override
			@Nullable
			public String convert(SkungeeServer server) {
				return server.getName();
			}
		});

		Converters.registerConverter(String.class, SkungeeServer.class, new Converter<String, SkungeeServer>() {
			@Override
			@Nullable
			public SkungeeServer convert(String name) {
				return new SkungeeServerMapper().apply(name);
			}
		});

		// SkungeePlayer
		Converters.registerConverter(SkungeePlayer.class, OfflinePlayer.class, new Converter<SkungeePlayer, OfflinePlayer>() {
			@Override
			@Nullable
			public OfflinePlayer convert(SkungeePlayer player) {
				return Bukkit.getOfflinePlayer(player.getUniqueId());
			}
		});

		Converters.registerConverter(OfflinePlayer.class, SkungeePlayer.class, new Converter<OfflinePlayer, SkungeePlayer>() {
			@Override
			@Nullable
			public SkungeePlayer convert(OfflinePlayer player) {
				return new SkungeePlayerMapper().apply(player);
			}
		});

		Converters.registerConverter(String.class, SkungeePlayer.class, new Converter<String, SkungeePlayer>() {
			@Override
			@Nullable
			public SkungeePlayer convert(String name) {
				return new SkungeePlayerMapper().apply(name);
			}
		});

	}

}
