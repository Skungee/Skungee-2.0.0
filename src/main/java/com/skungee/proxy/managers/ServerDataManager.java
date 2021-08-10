package com.skungee.proxy.managers;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.sitrica.japson.gson.JsonArray;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Packet;
import com.skungee.proxy.ProxyPlatform;
import com.skungee.proxy.ProxySkungee;
import com.skungee.shared.Packets;
import com.skungee.shared.objects.ServerData;
import com.skungee.shared.objects.SkungeeServer;

public class ServerDataManager {

	private final static Map<InetSocketAddress, ServerData> map = new HashMap<>();
	private final ProxyPlatform platform;

	public ServerDataManager(ProxyPlatform platform) {
		this.platform = platform;
	}

	public final static Map<InetSocketAddress, ServerData> getServerDataMap() {
		return Collections.unmodifiableMap(map);
	}

	/**
	 * Grab a ServerData from the actual defined address in the config.yml of a sevrer.
	 * 
	 * @param address The server of the server to get or make.
	 * @return The ServerData found or generated.
	 */
	public Optional<ServerData> get(InetSocketAddress address) {
		for (Entry<InetSocketAddress, ServerData> entry : map.entrySet()) {
			if (address.equals(entry.getKey()))
				return Optional.ofNullable(entry.getValue());
			if (address.getHostName().equals("localhost") && entry.getKey().getHostName().equals("localhost") && address.getPort() == entry.getKey().getPort())
				return Optional.ofNullable(entry.getValue());
			if (address.getAddress().isAnyLocalAddress() && entry.getKey().getAddress().isAnyLocalAddress() && address.getPort() == entry.getKey().getPort())
				return Optional.ofNullable(entry.getValue());
			if (address.getHostString().equals(entry.getKey().getHostString()) && address.getPort() == entry.getKey().getPort())
				return Optional.ofNullable(entry.getValue());
		}
		return Optional.empty();
	}

	public void set(InetSocketAddress address, ServerData data) {
		map.put(address, data);
		//checkGlobalScripts(data);
	}

	public void checkGlobalScripts(ServerData data) {
		File scripts = platform.getScriptsDirectory();
		if (!scripts.isDirectory() || scripts.listFiles().length <= 0 || !data.hasReceiver())
			return;
		SkungeeServer server = ProxySkungee.getPlatform().getServer(data.getAddress())
				.orElseThrow(() -> new IllegalStateException("There was no server found under " + data.getAddress()
						+ " but server data exists? Please report this on the Skungee GitHub."));
		String charset = platform.getPlatformConfiguration().getScriptsCharset();
		Charset chars = Charset.forName("UTF-8");
		if (!charset.equals("default"))
			chars = Charset.forName(charset);
		Multimap<String, String> map = HashMultimap.create();
		// Find all scripts.
		for (File file : scripts.listFiles()) {
			try {
				if (file.isDirectory()) {
					if (file.getName().equalsIgnoreCase(server.getName())) {
						for (File script : file.listFiles()) {
							if (script.isDirectory()) {
								platform.consoleMessage("Skungee will not read directories inside server directories, skipping " + script.getAbsolutePath());
								continue;
							}
							map.putAll(script.getName(), Files.readAllLines(script.toPath(), chars));
						}
					}
					continue;
				}
				map.putAll(file.getName(), Files.readAllLines(file.toPath(), chars));
			} catch (IOException e) {
				platform.consoleMessage("Charset " + charset + " does not support some symbols in script " + file.getAbsolutePath());
				e.printStackTrace();
			}
		}
		// No scripts found. Unlikely condition.
		if (map.isEmpty())
			return;
		// Remove scripts that don't need updating.
		Multimap<String, String> existing = HashMultimap.create();
		existing.putAll(data.getScripts());
		for (Entry<String, Collection<String>> existingEntry : existing.asMap().entrySet()) {
			String current = "";
			for (Entry<String, Collection<String>> entry : map.asMap().entrySet()) {
				current = existingEntry.getKey();
				if (current.equalsIgnoreCase(entry.getKey())) {
					// If this script matches what is currently on the Spigot, then ignore this script.
					if (existingEntry.getValue().equals(entry.getValue())) {
						existing.removeAll(current);
						continue;
					}
				}
			}
		}
		// If this is empty, that means that all the scripts on the Spigot already match that of what's on the Proxy.
		if (existing.isEmpty())
			return;
		// Setup the sending multimap for the correct scripts.
		Multimap<String, String> send = HashMultimap.create();
		for (Entry<String, String> entry : map.entries()) {
			if (!existing.containsKey(entry.getKey()))
				continue;
			send.put(entry.getKey(), entry.getValue());
		}
		// Send the packet.
		try {
			platform.getJapsonServer().sendPacket(data.getJapsonAddress(), new Packet(Packets.GLOBAL_SCRIPTS.getPacketId()) {
				@Override
				public JsonObject toJson() {
					JsonObject object = new JsonObject();
					JsonArray array = new JsonArray();
					for (Entry<String, Collection<String>> entry : send.asMap().entrySet()) {
						JsonObject script = new JsonObject();
						script.addProperty("name", entry.getKey());
						JsonArray lines = new JsonArray();
						for (String line : entry.getValue())
							lines.add(line);
						script.add("lines", lines);
						array.add(lines);
					}
					object.add("scripts", array);
					return object;
				}
			});
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			platform.debugMessage("Failed to send global scripts packet: " + e.getLocalizedMessage());
		}
	}

}
