package com.skungee.spigot.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.skungee.shared.objects.SkungeePlayer;

public class PreLoginEvent extends Event implements Cancellable {

	private final SkungeePlayer player;
	private boolean cancelled;

	public PreLoginEvent(SkungeePlayer player) {
		super(true);
		this.player = player;
	}

	public SkungeePlayer getPlayer() {
		return player;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	// Bukkit stuff
	private final static HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
