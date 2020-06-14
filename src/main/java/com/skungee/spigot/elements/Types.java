package com.skungee.spigot.elements;

import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;

import com.skungee.shared.objects.SkungeePlayer;
import com.skungee.shared.objects.SkungeeServer;
import com.skungee.spigot.SpigotSkungee;
import com.skungee.spigot.managers.ServerManager;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;

public class Types {

	static {
		Classes.registerClass(new ClassInfo<>(SkungeeServer.class, "skungeeserver")
				.user("(skungee)?servers?")
				.name("Skungee Server")
				.defaultExpression(new EventValueExpression<>(SkungeeServer.class))
				.parser(new Parser<SkungeeServer>() {

					@Override
					@Nullable
					public SkungeeServer parse(String input, ParseContext context) {
						if (!Pattern.compile("^[A-Za-z0-9\\s]+$").matcher(input).matches())
							return null;
						Optional<SkungeeServer> server = ServerManager.getServer(input);
						if (server.isPresent())
							return server.get();
						return null;
					}

					@Override
					public boolean canParse(ParseContext context) {
						return SpigotSkungee.getInstance().getConfig().getBoolean("skungee-server-parsing", false);
					}

					@Override
					public String toString(SkungeeServer server, int flags) {
						return server.getName();
					}

					@Override
					public String toVariableNameString(SkungeeServer server) {
						return server.getName();
					}

					@Override
					public String getVariableNamePattern() {
						return "\\S+";
					}

				}));
		Classes.registerClass(new ClassInfo<>(SkungeePlayer.class, "skungeeplayer")
				.user("skungeeplayers?")
				.name("Skungee Player")
				.defaultExpression(new EventValueExpression<>(SkungeePlayer.class))
				.parser(new Parser<SkungeePlayer>() {

					@Override
					@Nullable
					public SkungeePlayer parse(String input, ParseContext context) {
						return null;
					}

					@Override
					public boolean canParse(ParseContext context) {
						return false;
					}

					@Override
					public String toString(SkungeePlayer player, int flags) {
						return player.getName();
					}

					@Override
					public String toVariableNameString(SkungeePlayer player) {
						return player.getName();
					}

					@Override
					public String getVariableNamePattern() {
						return "\\S+";
					}

				}));
	}

}
