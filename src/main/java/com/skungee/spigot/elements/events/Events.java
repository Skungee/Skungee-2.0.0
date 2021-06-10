package com.skungee.spigot.elements.events;

import com.skungee.spigot.events.SignalReceiveEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.util.SimpleEvent;

public class Events {

	static {
		Skript.registerEvent("signal", SimpleEvent.class, SignalReceiveEvent.class, "signal[s] receive");
	}

}
