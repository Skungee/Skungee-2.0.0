package com.skungee.spigot.lang;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.skungee.spigot.SpigotSkungee;

import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.log.SkriptLogger;

/**
 * A Skript event that will read the first tier of syntaxes in an event.
 * <p> Use the provided nodes and cast to SectionNode for grabbing sections such as conditions.
 * 
 * @author LimeGlass
 */
public abstract class AdvancedReadingSkriptEvent extends SelfRegisteringSkriptEvent {

	private final static Table<Trigger, SectionNode, Boolean> cancellations = HashBasedTable.create();
	private final static Table<Trigger, List<Node>, List<String>> table = HashBasedTable.create();
	private final Class<? extends Event>[] events;
	private final boolean stripComments;

	private boolean registeredExecutor = false;

	/**
	 * If the comments of the syntaxes should be stripped. Setting to false will eliminate reflection.
	 * 
	 * @param stripComments strip the comments from the syntaxes.
	 */
	@SafeVarargs
	public AdvancedReadingSkriptEvent(boolean stripComments, Class<? extends Event>... events) {
		this.stripComments = stripComments;
		this.events = events;
	}

	@SuppressWarnings("unchecked")
	private <T> T getField(Class<?> from, Object object, String field) {
		try {
			Field f = from.getDeclaredField(field);
			f.setAccessible(true);
			return (T) f.get(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	protected abstract void onLoad(Trigger trigger);

	protected abstract void onUnload(Trigger trigger);

	private void registerExecutor() {
		if (registeredExecutor)
			return;
		for (Class<? extends Event> event : events)
			Bukkit.getPluginManager().registerEvent(event, new Listener() {}, EventPriority.NORMAL, new EventExecutor() {
				@Override
				public void execute(@Nullable Listener listener, @Nullable Event event) throws EventException {
					if (event == null)
						return;
					if (!accept(event))
						return;
					SkriptEventHandler.logEventStart(event);
					for (Trigger trigger : table.rowKeySet()) {
						SkriptEventHandler.logTriggerStart(trigger);
						trigger.execute(event);
						SkriptEventHandler.logTriggerEnd(trigger);
					}
					SkriptEventHandler.logEventEnd();
				}
			}, SpigotSkungee.getInstance(), true);
		registeredExecutor = true;
	}

	protected abstract boolean accept(Event event);

	@Override
	public void register(Trigger trigger) {
		Node node = SkriptLogger.getNode();
		if (node == null || !(node instanceof SectionNode))
			return;
		registerExecutor();
		SectionNode section = (SectionNode) node;
		List<Node> nodes = Lists.newArrayList(section);
		List<String> syntaxes = new ArrayList<>();
		boolean cancel = false;
		for (Node element : section) {
			String syntax = element.getKey().toLowerCase(Locale.US);
			if (stripComments) {
				String comment = getField(Node.class, node, "comment");
				if (comment != null)
					syntax.replaceAll(Pattern.quote(comment), "").trim();
			}
			syntaxes.add(syntax);
			if (syntax.startsWith("cancel the event") || syntax.startsWith("cancel event"))
				cancel = true;
		}
		cancellations.put(trigger, section, cancel);
		table.put(trigger, nodes, syntaxes);
		onLoad(trigger);
	}

	@Override
	public void unregister(Trigger trigger) {
		cancellations.remove(trigger, cancellations.row(trigger).keySet());
		table.remove(trigger, table.row(trigger).keySet());
		onUnload(trigger);
	}

	@Override
	public void unregisterAll() {
		cancellations.clear();
		table.clear();
	}

	protected Set<Trigger> getTriggers() {
		return cancellations.rowKeySet();
	}

	protected boolean wantsToCancelEvent(Trigger trigger) {
		return cancellations.get(trigger, cancellations.row(trigger).keySet());
	}

	protected List<Node> getNodes(Trigger trigger) {
		return table.row(trigger).keySet().stream().flatMap(set -> set.stream()).collect(Collectors.toList());
	}

	protected List<String> getSyntaxes(Trigger trigger) {
		return table.row(trigger).values().stream().flatMap(collection -> collection.stream()).collect(Collectors.toList());
	}

}
