package com.skungee.shared;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.Nullable;

import com.skungee.shared.objects.SkungeePlayer;
import com.skungee.shared.objects.SkungeeServer;

public interface Platform {

	@Nullable
	public SkungeeServer getCurrentServer(SkungeePlayer player);

	public Optional<SkungeeServer> getServer(String name);

	public Set<SkungeeServer> getServers();

	public Optional<SkungeePlayer> getPlayer(String name);

	public Optional<SkungeePlayer> getPlayer(UUID uuid);

	public Set<SkungeePlayer> getPlayers();

}
