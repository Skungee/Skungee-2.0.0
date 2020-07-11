package com.skungee.shared;

public enum Packets {

	// 0x00 is for Japson's heartbeats internally.
	/**
	 * Reserved for API requests.
	 */
	API(0x01),
	PLAYERS(0x02),
	SERVERS(0x03),
	CONNECT(0x04),
	VARIABLES(0x05),
	/**
	 *  SERVER_DATA is for data exclusive to Spigot, like whitelisted players, max players, etc.
	 *  This sends that data in a packet to the proxy.
	 */
	SERVER_DATA(0x06);

	private final int id;

	private Packets(int id) {
		this.id = id;
	}

	public int getPacketId() {
		return id;
	}

}
