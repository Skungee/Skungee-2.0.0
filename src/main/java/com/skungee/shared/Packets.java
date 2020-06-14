package com.skungee.shared;

public enum Packets {

	API(0x01),
	PLAYERS(0x02),
	SERVERS(0x03),
	CONNECT(0x04);

	private final int id;

	private Packets(int id) {
		this.id = id;
	}

	public int getPacketId() {
		return id;
	}

}
