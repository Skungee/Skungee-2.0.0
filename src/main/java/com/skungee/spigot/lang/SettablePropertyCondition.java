package com.skungee.spigot.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.conditions.base.PropertyCondition;

public abstract class SettablePropertyCondition<T> extends PropertyCondition<T> {

	public enum SettableConditionType {
		SET,
		SET_MAKE,
		TOGGLE
	}

	public static void register(Class<? extends SettablePropertyCondition<?>> c, PropertyType propertyType, SettableConditionType settableType, String property, String type) {
		if (type.contains("%"))
			throw new SkriptAPIException("The type argument must not contain any '%'s");
		PropertyCondition.register(c, propertyType, property, type);
		switch (settableType) {
			case SET:
				Skript.registerEffect(null, "set " + type + " of %" + property + "% to %boolean%");
				break;
			case SET_MAKE:
				Skript.registerEffect(null, "(set|make) " + type + " of %" + property + "% to %boolean%");
				break;
			case TOGGLE:
				Skript.registerEffect(null, "toggle " + type + " of %" + property + "%");
				break;
			default:
				assert false;
		}
	}

	protected abstract void change(T value, boolean change);

}
