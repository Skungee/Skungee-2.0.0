package com.skungee.spigot.elements;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.eclipse.jdt.annotation.Nullable;

import com.skungee.shared.objects.SkungeePlayer;
import com.skungee.shared.objects.SkungeeServer;

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

		// SkungeePlayer
		Converters.registerConverter(SkungeePlayer.class, OfflinePlayer.class, new Converter<SkungeePlayer, OfflinePlayer>() {
			@Override
			@Nullable
			public OfflinePlayer convert(SkungeePlayer player) {
				return Bukkit.getOfflinePlayer(player.getUniqueId());
			}
		});

	}

}
