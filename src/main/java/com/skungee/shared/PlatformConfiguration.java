package com.skungee.shared;

import java.net.InetSocketAddress;

public interface PlatformConfiguration {

	public Integer[] getIgnoredDebugPackets();

	public InetSocketAddress getBindAddress();

	public int getConfigurationVersion();

	public String getScriptsCharset();

	public int getBufferSize();

	public boolean isDebug();

}
