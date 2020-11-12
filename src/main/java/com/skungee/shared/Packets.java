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
	SIGNAL(0x02),
	PLAYERS(0x03),
	SERVERS(0x04),
	CONNECT(0x05),
	MESSAGE(0x06),
	VARIABLES(0x07),
	/**
	 *  SERVER_DATA is exclusive for Spigot to Proxy.
	 *  This packet sends server data like whitelisted players, max players, etc, in a packet to the proxy.
	 */
	SERVER_DATA(0x08),
	/**
	 *  GLOBAL_SCRIPTS is exclusive for Proxy to Spigot.
	 *  This sends scripts that need to be updated and reloaded.
	 */
	GLOBAL_SCRIPTS(0x09);

	private final int id;

	private Packets(int id) {
		this.id = id;
	}

	public int getPacketId() {
		return id;
	}

}
