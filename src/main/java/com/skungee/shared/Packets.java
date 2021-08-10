package com.skungee.shared;

public enum Packets {

	/*
	 *  0x00 is for Japson's heartbeats internally.
	 */
	HEARTBEAT(0x00),
	/**
	 * Reserved for API requests.
	 */
	API(0x01),
	EVENT(0x02),
	SIGNAL(0x03),
	PLAYERS(0x04),
	SERVERS(0x05),
	CONNECT(0x06),
	MESSAGE(0x07),
	VARIABLES(0x08),
	/**
	 *  SERVER_DATA is exclusive for Spigot to Proxy.
	 *  This packet sends server data like whitelisted players, max players, etc, in a packet to the proxy.
	 */
	SERVER_DATA(0x09),
	/**
	 * Used internally to notify the Proxy to setup callbacks.
	 */
	CANCELLATION(0x10),
	SERVER_COMMAND(0x11),
	/**
	 *  GLOBAL_SCRIPTS is exclusive for Proxy to Spigot.
	 *  This sends scripts that need to be updated and reloaded.
	 */
	GLOBAL_SCRIPTS(0x12),
	PROXY_PLAYER_COMMAND(0x13);

	private final int id;

	private Packets(int id) {
		this.id = id;
	}

	public int getPacketId() {
		return id;
	}

}
