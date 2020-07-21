package com.skungee.shared;

public enum Packets {

	// 0x00 is for Japson's heartbeats internally.
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
	 *  SERVER_DATA is for data exclusive to Spigot, like whitelisted players, max players, etc.
	 *  This sends that data in a packet to the proxy.
	 */
	SERVER_DATA(0x08);

	private final int id;

	private Packets(int id) {
		this.id = id;
	}

	public int getPacketId() {
		return id;
	}

}
