package com.skungee.spigot.managers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;

public class SyntaxInjector {

	private static void acceptRegistrations(boolean allow) throws NoSuchFieldException, IllegalAccessException {
		Field field = Skript.class.getDeclaredField("acceptRegistrations");
		field.setAccessible(true);
		field.set(null, allow);
	}

	public static <T extends Event> void registerEffect(EffectInjector<T> effect) {
		try {
			acceptRegistrations(true);
			Skript.registerEffect(effect.getClass(), effect.getSyntaxes());
			acceptRegistrations(false);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			Skript.error("Could not register effect " + effect.getName());
		}
	}

	public static class InjectorHelper<T extends Event> {

		private final ListMultimap<ClassInfo<?>, Expression<?>> parsed;
		private final T event;

		public InjectorHelper(T event, ListMultimap<ClassInfo<?>, Expression<?>> parsed) {
			this.parsed = parsed;
			this.event = event;
		}

		public final <P> Optional<Expression<P>> getExpression(Class<P> c, int index) {
			@SuppressWarnings("unchecked")
			Expression<P> expression = (Expression<P>) parsed.get(Classes.getExactClassInfo(c)).get(index);
			return Optional.ofNullable(expression);
		}

		public final T getEvent() {
			return event;
		}

	}

	public static abstract class EffectInjector<T extends Event> extends Effect {

		private final ListMultimap<ClassInfo<?>, Expression<?>> parsed = MultimapBuilder.hashKeys().arrayListValues().build();
		private final List<ClassInfo<?>> types = new ArrayList<>();
		private final Consumer<InjectorHelper<T>> consumer;
		private final String[] syntaxes;
		private final String name;

		public EffectInjector(String name, Consumer<InjectorHelper<T>> consumer, String... syntaxes) {
			this.syntaxes = syntaxes;
			this.consumer = consumer;
			this.name = name;
			for (String syntax : syntaxes) {
				String[] types = StringUtils.substringsBetween(syntax, "%", "%");
				if (types == null) // Simple Effect.
					continue;
				for (String type : types) {
					if (!type.contains("/"))
						continue;
					type = "object";
					ClassInfo<?> classInfo = Classes.getClassInfoNoError(type);
					if (classInfo != null)
						this.types.add(classInfo);
				}
			}
		}

		@Override
		public final boolean init(Expression<?>[] expresssions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
			int i = 0;
			for (Expression<?> expression : expresssions) {
				ClassInfo<?> type = types.get(i);
				if (type == null) {// Incorrect amount of types?
					Skript.error("Incorrect amount of types compared to the predicted amount.", ErrorQuality.SEMANTIC_ERROR);
					return false;
				}
				if (expression.getReturnType().equals(type.getC()))
					parsed.put(type, expression);
				i++;
			}
			return parsed.size() == expresssions.length;
		}

		@Override
		public String toString(@Nullable Event e, boolean debug) {
			return name;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected final void execute(Event event) {
			consumer.accept(new InjectorHelper<T>((T) event, parsed));
		}

		public String[] getSyntaxes() {
			return syntaxes;
		}

		public String getName() {
			return name;
		}

	}

}
