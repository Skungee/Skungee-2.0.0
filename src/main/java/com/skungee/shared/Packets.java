package com.skungee.shared;

public enum Packets {

	/*
	 *  0 is for Japson's heartbeats internally.
	 */
	HEARTBEAT(0),
	/**
	 * Reserved for API requests.
	 */
	API(1),
	EVENT(2),
	SIGNAL(3),
	PLAYERS(4),
	SERVERS(5),
	CONNECT(6),
	MESSAGE(7),
	ACTIONBAR(8),
	VARIABLES(9),
	/**
	 *  SERVER_DATA is exclusive for Spigot to Proxy.
	 *  This packet sends server data like whitelisted players, max players, etc, in a packet to the proxy.
	 */
	SERVER_DATA(10),
	/**
	 * Used internally to notify the Proxy to setup callbacks.
	 */
	CANCELLATION(11),
	SERVER_COMMAND(12),
	/**
	 *  GLOBAL_SCRIPTS is exclusive for Proxy to Spigot.
	 *  This sends scripts that need to be updated and reloaded.
	 */
	GLOBAL_SCRIPTS(13),
	PROXY_PLAYER_COMMAND(14);

	private final byte id;

	private Packets(int id) {
		this.id = (byte) id;
	}

	public byte getPacketId() {
		return id;
	}

}
