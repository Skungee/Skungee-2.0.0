package com.skungee.shared.objects;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class ServerData {

	private final Multimap<String, String> scripts = HashMultimap.create();
	private final InetSocketAddress japsonAddress, serverAddress;
	private Set<UUID> whitelisted = new HashSet<>();
	private InetSocketAddress receiver;
	private String motd, version;
	private int limit;

	public ServerData(InetSocketAddress serverAddress, InetSocketAddress japsonAddress) {
		this.serverAddress = serverAddress;
		this.japsonAddress = japsonAddress;
	}

	public Multimap<String, String> getScripts() {
		return scripts;
	}

	public void addScript(String name, List<String> lines) {
		this.scripts.putAll(name, lines);
	}

	public InetSocketAddress getAddress() {
		return serverAddress;
	}

	public InetSocketAddress getJapsonAddress() {
		return japsonAddress;
	}

	public InetSocketAddress getReceiverAddress() throws IllegalStateException {
		if (!hasReceiver())
			throw new IllegalStateException("The server data from address " + serverAddress.getHostName() + ":" + serverAddress.getPort() + " does not have a receiver!");
		return receiver;
	}

	public String getMotd() {
		return motd;
	}
//
//	public SkungeeServer getServer() {
//		if (!(Skungee.getPlatform() instanceof ProxyPlatform))
//			throw new IllegalStateException("The method getServer can only be called from a Proxy in ServerData");
//		return ((ProxyPlatform)Skungee.getPlatform()).getServer(serverAddress)
//				.orElseThrow(() -> new IllegalStateException("There was no server found under " + serverAddress
//						+ " but server data exists? Please report this on the Skungee GitHub."));
//	}

	public void setWhitelisted(Set<UUID> whitelisted) {
		this.whitelisted = whitelisted;
	}

	public Set<UUID> getWhitelisted() {
		return whitelisted;
	}

	public void setReceiverAddress(InetSocketAddress receiver) {
		this.receiver = receiver;
	}

	public void setMotd(String motd) {
		this.motd = motd;
	}

	public boolean hasReceiver() {
		return receiver != null;
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
