package com.skungee.spigot.handlers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;

import com.sitrica.japson.gson.JsonArray;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Executor;
import com.skungee.shared.Packets;
import com.skungee.shared.Skungee;

import ch.njol.skript.Skript;

public class ScriptsHandler extends Executor {

	public ScriptsHandler() {
		super(Packets.GLOBAL_SCRIPTS.getPacketId());
	}

	@Override
	public void execute(InetAddress address, int port, JsonObject object) {
		Skungee.getPlatform().debugMessage("Received Global Script Packet");
		if (!object.has("scripts"))
			return;
		File scriptsFolder = new File(Skript.getInstance().getDataFolder().getAbsolutePath() + "/" + Skript.SCRIPTSFOLDER + "/Global");
		JsonArray scripts = object.get("scripts").getAsJsonArray();
		scripts.forEach(element -> {
			JsonObject script = element.getAsJsonObject();
			if (!script.has("name") || !script.has("lines"))
				return;
			String name = script.get("name").getAsString();

			String base64Encoded = script.get("lines").getAsString();
			byte[] decoded = Base64.getDecoder().decode(base64Encoded);

			File file;
			try {
				file = File.createTempFile("Skungee", name);
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(decoded);
				fos.close();
				// I don't care if you are there
//				Optional<File> existing = Stream.of(scriptsFolder.listFiles(new FilenameFilter() {
//					@Override
//					public boolean accept(File dir, String name) {
//						return name.toLowerCase().equals(name.toLowerCase());
//					}
//				})).findFirst();
//				if (!existing.isPresent()) {
//					file.delete();
//					return;
//				}
//				Files.deleteIfExists(existing.get().toPath());
				File newScript = new File(scriptsFolder + File.separator + name);
				com.google.common.io.Files.move(file, newScript);
				Bukkit.getScheduler().runTask(Skript.getInstance(),() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sk reload Global/" + name));
				file.delete();
				Skungee.getPlatform().debugMessage("Reloaded Global Script " + name);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

}
