package com.skungee.spigot.tasks;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.bukkit.Bukkit;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.sitrica.japson.client.JapsonClient;
import com.sitrica.japson.gson.JsonArray;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Packet;
import com.skungee.shared.Packets;
import com.skungee.spigot.SpigotSkungee;

import ch.njol.skript.Skript;

public class ServerDataTask implements Runnable {

	private final SpigotSkungee instance;
	private final JapsonClient japson;

	public ServerDataTask(SpigotSkungee instance, JapsonClient japson) {
		this.instance = instance;
		this.japson = japson;
	}

	private final Multimap<String, String> getScripts(File directory) {
		Multimap<String, String> map = HashMultimap.create();
		if (directory == null)
			directory = new File(Skript.getInstance().getDataFolder().getAbsolutePath() + File.separator + Skript.SCRIPTSFOLDER);
		Charset charset = Charset.forName(instance.getPlatformConfiguration().getScriptsCharset());
		Arrays.stream(directory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".sk") && !name.startsWith("-");
			}
		})).forEach(script -> {
			try {
				if (script.isDirectory()) {
					map.putAll(getScripts(script));
					return;
				}
				map.putAll(script.getName(), Files.readAllLines(script.toPath(), charset));
			} catch (IOException e) {
				instance.consoleMessage("Charset " + charset + " does not support some symbols in script " + script.getAbsolutePath());
				e.printStackTrace();
			}
		});
		return map;
	}

	@Override
	public void run() {
		try {
			japson.sendPacket(new Packet(Packets.SERVER_DATA.getPacketId()) {
				@Override
				public JsonObject toJson() {
					JsonObject object = new JsonObject();
					object.addProperty("japson-address", japson.getAddress().getHostName());
					object.addProperty("japson-port", japson.getPort());
					object.addProperty("limit", Bukkit.getMaxPlayers());
					object.addProperty("version", Bukkit.getVersion());
					object.addProperty("address", Bukkit.getIp());
					object.addProperty("motd", Bukkit.getMotd());
					object.addProperty("port", Bukkit.getPort());
					JsonArray whitelisted = new JsonArray();
					Bukkit.getWhitelistedPlayers().forEach(player -> whitelisted.add(player.getUniqueId() + ""));
					object.add("whitelisted", whitelisted);
					JsonArray banned = new JsonArray();
					Bukkit.getBannedPlayers().forEach(player -> banned.add(player.getUniqueId() + ""));
					object.add("banned", banned);
					JsonArray operators = new JsonArray();
					Bukkit.getOperators().forEach(player -> operators.add(player.getUniqueId() + ""));
					object.add("operators", operators);
					instance.getReceiver().ifPresent(receiver -> object.addProperty("receiver-port", receiver.getPort()));

					File scriptsFolder = new File(Skript.getInstance().getDataFolder().getAbsolutePath() + File.separator + Skript.SCRIPTSFOLDER);
					Multimap<String, String> map = getScripts(scriptsFolder);
					JsonArray array = new JsonArray();
					for (Entry<String, Collection<String>> entry : map.asMap().entrySet()) {
						JsonObject script = new JsonObject();
						script.addProperty("name", entry.getKey());
						JsonArray lines = new JsonArray();
						for (String line : entry.getValue())
							lines.add(line);
						script.add("lines", lines);
						array.add(script);
					}
					object.add("scripts", array);
					return object;
				}
			});
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
		}
	}

}
