package com.skungee.proxy;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.sitrica.japson.gson.JsonArray;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Packet;
import com.skungee.shared.Packets;
import com.skungee.shared.Skungee;
import com.skungee.shared.objects.SkungeeServer;

public class ServerDataManager {

	private final static Map<InetSocketAddress, ServerData> map = new HashMap<>();

	/**
	 * Grab a ServerData from the actual defined address in the config.yml of a sevrer.
	 * 
	 * @param address The server of the server to get or make.
	 * @return The ServerData found or generated.
	 */
	public static Optional<ServerData> get(InetSocketAddress address) {
		return Optional.ofNullable(map.get(address));
	}

	public static void set(InetSocketAddress address, ServerData data) {
		map.put(address, data);
		checkGlobalScripts(data);
	}

	public static void checkGlobalScripts(ServerData data) {
		ProxyPlatform platform = (ProxyPlatform) Skungee.getPlatform();
		File scripts = platform.getScriptsDirectory();
		if (!scripts.isDirectory() || scripts.listFiles().length <= 0 || !data.hasReceiver())
			return;
		SkungeeServer server = data.getServer();
		String charset = platform.getPlatformConfiguration().getScriptsCharset();
		Charset chars = Charset.defaultCharset();
		if (!charset.equals("default"))
			chars = Charset.forName(charset);
		HashMap<String, String> map = new HashMap<String, String>();
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
							map.put(script.getName(), Base64.getEncoder().encodeToString(Files.readAllBytes(script.toPath())));
						}
					}
					continue;
				}
				map.put(file.getName(), Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath())));
			} catch (IOException e) {
				platform.consoleMessage("Charset " + charset + " does not support some symbols in script " + file.getAbsolutePath());
				e.printStackTrace();
			}
		}
		// No scripts found. Unlikely condition.
		if (map.isEmpty())
			return;
		// Remove scripts that don't need updating.
		HashMap<String, String> newMap = new HashMap<String, String>();
		newMap.putAll(map);
		HashMap<String, String> existing = new HashMap<String, String>();
		existing.putAll(data.getScripts());
		for(String bungeeKey : map.keySet()) {
			for(String serverKey : existing.keySet()) {
				if(bungeeKey.equalsIgnoreCase(serverKey)) {
					if(map.get(bungeeKey).equals(existing.get(serverKey))) {
						newMap.remove(bungeeKey);
						continue;
					}
				}
			}
		}
		// Don't send when there's nothing to update
		if(newMap.isEmpty()) return;
		// Send the packet.
		try {
			Skungee.getPlatform().debugMessage("Send GlobalScript Packet to "+data.getJapsonAddress());
			platform.getJapsonServer().sendPacket(new InetSocketAddress(data.getJapsonAddress().getAddress(), data.getReceiverPort()), new Packet(Packets.GLOBAL_SCRIPTS.getPacketId()) {
				@Override
				public JsonObject toJson() {
					JsonObject object = new JsonObject();
					JsonArray scriptsArray = new JsonArray();
					for (String key : newMap.keySet()) {
						JsonObject script = new JsonObject();
						script.addProperty("name", key);
						script.addProperty("lines", newMap.get(key));
						scriptsArray.add(script);
					}
					object.add("scripts", scriptsArray);
					return object;
				}
			});
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			platform.debugMessage("Failed to send global scripts packet: " + e.getLocalizedMessage());
		}
	}

	public static class ServerData {

		private final HashMap<String, String> scripts = new HashMap<String, String>();
		private final InetSocketAddress japsonAddress, serverAddress;
		private Set<UUID> whitelisted = new HashSet<>();
		private String motd, version;
		private Integer receiverPort;
		private int limit;

		public ServerData(InetSocketAddress serverAddress, InetSocketAddress japsonAddress) {
			this.serverAddress = serverAddress;
			this.japsonAddress = japsonAddress;
		}

		public HashMap<String, String> getScripts() {
			return scripts;
		}

		public void addScript(String name, String lines) {
			this.scripts.put(name, lines);
		}

		public InetSocketAddress getAddress() {
			return serverAddress;
		}

		public InetSocketAddress getJapsonAddress() {
			return japsonAddress;
		}

		public String getMotd() {
			return motd;
		}

		public SkungeeServer getServer() {
			return ((ProxyPlatform)Skungee.getPlatform()).getServer(serverAddress)
					.orElseThrow(() -> new IllegalStateException("There was no server found under " + serverAddress
							+ " but server data exists? Please report this on the Skungee GitHub."));
		}

		public void setWhitelisted(Set<UUID> whitelisted) {
			this.whitelisted = whitelisted;
		}

		public Set<UUID> getWhitelisted() {
			return whitelisted;
		}

		public void setReceiverPort(int receiverPort) {
			this.receiverPort = receiverPort;
		}

		public void setMotd(String motd) {
			this.motd = motd;
		}

		public boolean hasReceiver() {
			return receiverPort != null;
		}

		public int getReceiverPort() {
			return receiverPort;
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

		public int getMaxPlayerLimit() {
			return limit;
		}

		public void setMaxPlayerLimit(int limit) {
			this.limit = limit;
		}

	}

}
