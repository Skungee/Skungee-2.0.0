package com.skungee.spigot.lang;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.SkriptLogger;

/**
 * A Skript event that will read the first tier of syntaxes in an event.
 * <p> Use the provided nodes and cast to SectionNode for grabbing sections such as conditions.
 * 
 * @author LimeGlass
 */
public abstract class ReadingSkriptEvent extends SkriptEvent {

	private final List<String> syntaxes = new ArrayList<>();
	private final List<Node> nodes = new ArrayList<>();
	private final boolean stripComments;
	private boolean cancelEvent;

	/**
	 * If the comments of the syntaxes should be stripped. Setting to false will eliminate reflection.
	 * 
	 * @param stripComments strip the comments from the syntaxes.
	 */
	public ReadingSkriptEvent(boolean stripComments) {
		this.stripComments = stripComments;
	}

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parser) {
		Node node = SkriptLogger.getNode();
		if (node == null || !(node instanceof SectionNode))
			return false;
		SectionNode section = (SectionNode) node;
		nodes.addAll(Lists.newArrayList(section));
		for (Node element : section) {
			String syntax = element.getKey().toLowerCase(Locale.US);
			if (stripComments) {
				String comment = getField(Node.class, node, "comment");
				if (comment != null)
					syntax.replaceAll(Pattern.quote(comment), "").trim();
			}
			syntaxes.add(syntax);
			if (syntax.startsWith("cancel the event") || syntax.startsWith("cancel event"))
				cancelEvent = true;
		}
		initialize(args, matchedPattern, parser);
		return true;
	}

	protected abstract boolean initialize(Literal<?>[] args, int matchedPattern, ParseResult parser);

	protected boolean wantsToCancelEvent() {
		return cancelEvent;
	}

	protected List<String> getSyntaxes() {
		return syntaxes;
	}

	protected List<Node> getNodes() {
		return nodes;
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

}
