package com.skungee.shared;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.skungee.shared.objects.SkungeePlayer;
import com.skungee.shared.objects.SkungeeServer;

public interface Platform {

	public Optional<SkungeeServer> getServer(String name);

	public Set<SkungeeServer> getServers();

	public Optional<SkungeePlayer> getPlayer(String name);

	public Optional<SkungeePlayer> getPlayer(UUID uuid);

	public Set<SkungeePlayer> getPlayers();

}
