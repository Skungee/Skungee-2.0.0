package com.skungee.spigot.elements.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.skungee.shared.objects.NetworkVariable;
import com.skungee.shared.objects.NetworkVariable.SkriptChangeMode;
import com.skungee.shared.objects.NetworkVariable.Value;
import com.skungee.spigot.SpigotSkungee;
import com.skungee.spigot.packets.NetworkVariablePacket;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

public class ExprNetworkVariable extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprNetworkVariable.class, Object.class, ExpressionType.SIMPLE, "(network|proxy) variable %objects%");
	}

	//private VariableString variableString;
	private Variable<?> variable;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		Expression<?> expression = expressions[0];
		if (expression instanceof Variable) {
			variable = (Variable<?>) expression;
		} else {
			if (expression instanceof UnparsedLiteral) {
				@SuppressWarnings("unchecked")
				Literal<?> parsedLiteral = ((UnparsedLiteral) expression).getConvertedExpression(Object.class);
				expression = parsedLiteral == null ? expression : parsedLiteral;
			}
			if (expression instanceof Variable)
				variable = (Variable<?>) expression;
		}
		if (variable == null) {
			Skript.error("Network variables must be in a variable format!");
			return false;
		}
		if (variable.isLocal()) {
			Skript.error("Network variables can not be a local variable.");
			return false;
		}
//		String name = StringUtils.substringBetween(variable.toString(), "{", "}");
//		// Creates a new VariableString which is what Skript accepts to get Variables.
//		variableString = VariableString.newInstance(name, StringMode.VARIABLE_NAME);
		return true;
	}

	@Override
	@Nullable
	protected Object[] get(Event event) {
		SpigotSkungee instance = SpigotSkungee.getInstance();
		List<NetworkVariable> variables = new ArrayList<>();;
		try {
			NetworkVariablePacket packet = new NetworkVariablePacket(new NetworkVariable[0])
					.setNames(variable.getName().toString(event));
			variables.addAll(instance.getJapsonClient().sendPacket(packet));
		} catch (TimeoutException | InterruptedException | ExecutionException e) {
			instance.consoleMessage("Timed out attempting to send network variable {" + variable.getName().toString(event) + "}");
		}
		return variables.stream()
				.flatMap(variable -> Arrays.stream(variable.getValues()))
				.map(value -> Classes.deserialize(value.type, value.data))
				.toArray(Object[]::new);
	}

	@Override
	public boolean isSingle() {
		return !variable.isList();
	}

	@Override
	public Class<? extends Object> getReturnType() {
		return Object.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (event == null)
			return "network variable";
		return "network variable " + variable.toString(event, debug);
	}

	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (isSingle() && (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.REMOVE_ALL)) {
			Skript.error("Skungee cannot " + mode.toString() + " values from a single variable. " + 
					"Skungee would have to send two communication packets, thus resulting in performance loss. Please get, modify and set to " + mode.toString()
					+ " single values if you insist on doing it this way.");
			return null;
		}
		return CollectionUtils.array(isSingle() ? Object.class : Object[].class);
	}

	@Override
	public void change(Event event, Object[] delta, ChangeMode mode) {
		SkriptChangeMode changer = SkriptChangeMode.valueOf(mode.toString());
		if (changer == null)
			return;
		Value[] values = null;
		if (delta != null) {
			values = new Value[delta.length];
			for (int i = 0; i < delta.length; i++) {
				ch.njol.skript.variables.SerializedVariable.Value value = Classes.serialize(delta[i]);
				values[i] = new Value(value.type, value.data);
			}
		}
		NetworkVariable variable = new NetworkVariable(this.variable.getName().toString(event), values);
		variable.setChanger(changer);
		SpigotSkungee instance = SpigotSkungee.getInstance();
		try {
			instance.getJapsonClient().sendPacket(new NetworkVariablePacket(variable));
		} catch (TimeoutException | InterruptedException | ExecutionException e) {
			instance.consoleMessage("Timed out attempting to send network variable {" + variable.getVariableString() + "}");
		}
	}

}
