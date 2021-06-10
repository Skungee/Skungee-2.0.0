package com.skungee.spigot.elements.events.expressions;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.skungee.spigot.events.SignalReceiveEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

public class ExprSignals extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprSignals.class, String.class, ExpressionType.SIMPLE, "[event-]signal[s] [messages]");
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!ParserInstance.get().isCurrentEvent(SignalReceiveEvent.class)) {
			Skript.error("The 'signals' event expression can only be used in a signal receive event.");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event event) {
		return ((SignalReceiveEvent)event).getSignals();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "signals";
	}

	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode != ChangeMode.REMOVE)
			return null;
		return CollectionUtils.array(String.class);
	}

	@Override
	public void change(Event event, Object[] delta, ChangeMode mode) {
		String[] signals = ((SignalReceiveEvent) event).getSignals();
		String remove = (String) delta[0];
		for (int i = 0; i < signals.length; i++) {
			if (signals[i].equals(remove)) {
				signals[i] = null;
				break;
			}
		}
	}

}
