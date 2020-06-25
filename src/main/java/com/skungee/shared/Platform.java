package com.skungee.shared;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.sitrica.japson.shared.Handler;
import com.skungee.shared.objects.SkungeePlayer;
import com.skungee.shared.objects.SkungeeServer;

public interface Platform {

	/**
	 * API access.
	 * 
	 * @param handler The Japson handler to register to the proxy.
	 * @throws IllegalAccessException if the packet id doesn't match that of Packets.API
	 */
	public void setApiHandler(Handler handler) throws IllegalAccessException;

	public Optional<SkungeeServer> getServer(String name);

	public Set<SkungeeServer> getServers();

	public Optional<SkungeePlayer> getPlayer(String name);

	public Optional<SkungeePlayer> getPlayer(UUID uuid);

	public Set<SkungeePlayer> getPlayers();

}
