package com.skungee.shared.objects;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.bukkit.OfflinePlayer;

import com.skungee.spigot.SpigotSkungee;
import com.skungee.spigot.packets.PlayersPacket;

public class SkungeePlayerMapper implements Function<Object, SkungeePlayer> {

	@Override
	public SkungeePlayer apply(Object object) {
		if (object instanceof SkungeePlayer)
			return (SkungeePlayer) object;
		PlayersPacket packet = new PlayersPacket();
		if (object instanceof String || object instanceof UUID) {
			if (object instanceof String) {
				packet.setNames((String) object);
			} else {
				packet.setUniqueIds((UUID) object);
			}
		}
		if (object.getClass().isAssignableFrom(OfflinePlayer.class)) {
			packet.setUniqueIds(((OfflinePlayer) object).getUniqueId());
		}
		try {
			Optional<SkungeePlayer> player = SpigotSkungee.getInstance().getJapsonClient().sendPacket(packet).stream().findFirst();
			if (!player.isPresent())
				return null;
			return player.get();
		} catch (TimeoutException | InterruptedException | ExecutionException e) {
			return null;
		}
	}

}
