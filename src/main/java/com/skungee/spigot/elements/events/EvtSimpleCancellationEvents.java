package com.skungee.spigot.elements.events;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.skungee.spigot.events.PreLoginEvent;
import com.skungee.spigot.lang.AdvancedReadingSkriptEvent;
import com.skungee.spigot.packets.CancellationPacket;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;

public class EvtSimpleCancellationEvents extends AdvancedReadingSkriptEvent {

	public EvtSimpleCancellationEvents() {
		super(false, PreLoginEvent.class);
		//Skript.registerEvent("pre login", EvtSimpleCancellationEvents.class, PreLoginEvent.class, "player pre(-| )login");
	}

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		return true;
	}

	@Override
	protected void onLoad(Trigger trigger) {
		if (!wantsToCancelEvent(trigger))
			return;
		try {
			new CancellationPacket(trigger.getName(), true).send();
		} catch (TimeoutException | InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onUnload(Trigger trigger) {
		for (Trigger other : getTriggers()) {
			if (wantsToCancelEvent(other))
				return;
		}
		try {
			new CancellationPacket(trigger.getName(), false).send();
		} catch (TimeoutException | InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected boolean accept(Event event) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "pre login";
	}

}
