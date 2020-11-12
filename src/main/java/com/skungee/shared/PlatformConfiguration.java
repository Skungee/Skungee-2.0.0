package com.skungee.shared;

public interface PlatformConfiguration {

	public Integer[] getIgnoredDebugPackets();

	public int getConfigurationVersion();

	public String getScriptsCharset();

	public String getBindAddress();

	public int getBufferSize();

	public boolean isDebug();

	public int getPort();

}
