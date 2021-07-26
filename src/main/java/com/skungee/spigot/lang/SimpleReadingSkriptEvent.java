package com.skungee.spigot.lang;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

public class SimpleReadingSkriptEvent extends ReadingSkriptEvent {

	public SimpleReadingSkriptEvent() {
		super(false);
	}

	@Override
	public boolean initialize(Literal<?>[] args, int matchedPattern, ParseResult parser) {
		if (args.length != 0)
			throw new SkriptAPIException("Invalid use of SimpleReadingSkriptEvent");
		return true;
	}

	@Override
	public boolean check(Event event) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "simple reading event";
	}

}
