package com.skungee.shared;

import java.io.File;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.skungee.shared.objects.SkungeePlayer;

public interface Platform {

	public PlatformConfiguration getPlatformConfiguration();

	public Optional<SkungeePlayer> getPlayer(String name);

	public Optional<SkungeePlayer> getPlayer(UUID uuid);

	public void consoleMessages(String... strings);

	public default void consoleMessage(String string) {
		consoleMessages(string);
	}

	public void debugMessages(Exception exception, String... strings);

	public void debugMessage(Exception exception, String... strings);

	public void debugMessages(String... strings);

	public default void debugMessage(String string) {
		debugMessages(string);
	}

	public void schedule(Runnable task, long delay, long period, TimeUnit unit);

	public void delay(Runnable task, long delay, TimeUnit unit);

	public Set<SkungeePlayer> getPlayers();

	public File getPlatformFolder();

}
