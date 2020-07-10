package com.skungee.proxy.variables.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.skungee.proxy.ProxyPlatform;
import com.skungee.proxy.variables.SkungeeStorage;
import com.skungee.shared.objects.NetworkVariable.Value;

public class FlatFileStorage extends SkungeeStorage {
	
	public FlatFileStorage(ProxyPlatform platform) {
		super(platform, "CSV", "flatfile");
	}

	private final String DELIMITER = "@: ";
	private boolean loadingHash = false;
	private File folder, file;
	private FileWriter writer;
	private Gson gson;
	
	private void header() throws IOException {
		writer.append("\n");
		writer.append("# Skungee's variable database.");
		writer.append("\n");
		writer.append("# Please do not modify this file manually, thank you!");
		writer.append("\n");
		writer.append("\n");
	}
	
	@Override
	public boolean initialize() {
		gson = new GsonBuilder().setLenient().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
		file = new File(variablesFolder + "variables.csv");
		folder = new File(variablesFolder);
		folder.mkdir();
		if (!file.exists()) {
			try {
				writer = new FileWriter(file);
				header();
				writer.flush();
				platform.debugMessage("Successfully created CSV variables database!");
			} catch (IOException e) {
				platform.consoleMessage("Failed to create a CSV variable database.");
				e.printStackTrace();
				return false;
			}
		} else load();
		return true;
	}
	
	@Override
	public Value[] get(String index) {
		if (index.endsWith("::*")) {
			ArrayList<Value> values = new ArrayList<Value>();
			index = index.substring(0, index.length() - 1);
			for (Entry<String, Value[]> entry : variables.entrySet()) {
				if (entry.getKey().startsWith(index)) {
					Value[] data = variables.get(entry.getKey());
					if (data == null) continue;
					for (Value value : data) {
						values.add(value);
					}
				}
			}
			Value[] data = variables.get(index);
			if (data != null) {
				for (Value value : data) {
					values.add(value);
				}
			}
			return values.toArray(new Value[values.size()]);
		}
		return variables.get(index);
	}

	@Override
	public void delete(String... indexes) {
		ArrayList<String> list = Lists.newArrayList(indexes);
		for (String index : indexes) {
			if (index.endsWith("::*")) {
				String varIndex = index.substring(0, index.length() - 1);
				for (Entry<String, Value[]> entry : variables.entrySet()) {
					if (entry.getKey().startsWith(varIndex)) {
						list.add(entry.getKey());
					}
				}
			}
		}
		try {
			writer.close();
		} catch (IOException e) {
			platform.consoleMessage("Failed to close writer for removing index");
			e.printStackTrace();
		}
		for (String index : list) {
			if (variables.containsKey(index) && !loadingHash) {
				variables.remove(index);
			}
		}
		loadFromHash();
	}
	
	@Override
	public void remove(Value[] objects, String... indexes) {
		ArrayList<String> list = Lists.newArrayList(indexes);
		for (String index : indexes) {
			if (index.endsWith("::*")) {
				index = index.substring(0, index.length() - 1);
				for (Entry<String, Value[]> entry : variables.entrySet()) {
					if (entry.getKey().startsWith(index)) {
						list.add(entry.getKey());
					}
				}
			}
		}
		try {
			writer.close();
		} catch (IOException e) {
			platform.consoleMessage("Failed to close writer for removing index");
			e.printStackTrace();
		}
		for (String index : list) {
			if (variables.containsKey(index) || !loadingHash) {
				Value[] v = variables.get(index);
				if (v == null) continue;
				ArrayList<Value> values = Lists.newArrayList(v);
				for (Value value : v) {
					for (Value object : objects) {
						if (value.isSimilar(object)) {
							values.remove(value);
						}
					}
				}
				if (values.isEmpty()) {
					variables.remove(index);
				} else {
					variables.put(index, values.toArray(new Value[values.size()]));
				}
			}
		}
		loadFromHash();
	}

	@Override
	public void backup() {
		//shut down the stream
		try {
			writer.close();
		} catch (IOException e) {
			platform.consoleMessage("Error closing the variable flatfile writter");
			e.printStackTrace();
		}
		Date date = new Date();
		new File(folder + File.separator + "backups" + File.separator).mkdir();
		File newFile = new File(folder + File.separator + "backups" + File.separator + date.toString().replaceAll(":", "-") + ".csv");
		try {
			Files.copy(file.toPath(), newFile.toPath());
		} catch (IOException e) {
			platform.consoleMessage("Failed to backup flatfile");
			e.printStackTrace();
		}
		load();
	}
	
	private void load() {
		String line = "";
		BufferedReader reader = null;
		try {
			//Key = index, Value = string serialized value.
			Map<String, String> map = new HashMap<String, String>();
			reader = new BufferedReader(new FileReader(file));
			//Skip the information at the top of the variables.csv file.
			for (int i = 0; i < 4; i ++) {
				reader.readLine();
			}
			while ((line = reader.readLine()) != null) {
				String[] values = line.split(DELIMITER, 2);
				if (values.length == 2) map.put(values[0], values[1]);
			}
			writer = new FileWriter(file);
			header();
			for (Entry<String, String> data : map.entrySet()) {
				Value[] values = gson.fromJson(data.getValue(), Value[].class);
				set(data.getKey(), values);
			}
			reader.close();
		} catch (IOException e) {
			platform.consoleMessage("Failed to load and write variables.");
			e.printStackTrace();
		}
	}
	
	@Override
	public void set(String name, Value[] values) {
//		if (Skungee.getConfig().getBoolean("NetworkVariables.AutomaticSharing", false)) {
//			if (!ServerTracker.isEmpty()) {
//				BungeeSockets.sendAll(new BungeePacket(false, BungeePacketType.UPDATEVARIABLES, name, values));
//			}
//		}
		if (variables.containsKey(name) && !loadingHash) {
			try {
				writer.close();
			} catch (IOException e) {
				platform.consoleMessage("Failed to close the writer while setting the value: " + name);
				e.printStackTrace();
			}
			variables.remove(name);
			loadFromHash();
		}
		variables.put(name, values);
		try {
			writer.append(name);
			writer.append(DELIMITER);
			writer.append(gson.toJson(values));
			writer.append("\n");
		} catch (IOException e) {
			try {
				writer = new FileWriter(file);
			} catch (IOException e1) {}
		} finally {
			try {
				writer.flush();
			} catch (IOException e) {
				platform.debugMessage("Error flushing data while writing!");
				e.printStackTrace();
			}
		}
	}
	
	private void loadFromHash() {
		loadingHash = true;
		try {
			writer = new FileWriter(file);
			header();
			if (!variables.isEmpty()) {
				ArrayList<String> ids = Lists.newArrayList(variables.keySet());
				Iterator<String> iterator = ids.iterator();
				while (iterator.hasNext()) {
					String ID = iterator.next();
					set(ID, variables.get(ID));
				}
			}
		} catch (IOException e) {
			platform.consoleMessage("Error flushing writer while loading from hash!");
			e.printStackTrace();
		} finally {
			try {
				writer.flush();
			} catch (IOException e) {
				platform.consoleMessage("Error flushing writer while loading from hash!");
				e.printStackTrace();
			}
		}
		loadingHash = false;
	}

}
