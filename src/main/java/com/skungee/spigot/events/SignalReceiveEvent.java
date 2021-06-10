package com.skungee.spigot.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SignalReceiveEvent extends Event {

	private final String[] signals;

	public SignalReceiveEvent(String... signals) {
		super(true);
		this.signals = signals;
	}

	public String[] getSignals() {
		return signals;
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
