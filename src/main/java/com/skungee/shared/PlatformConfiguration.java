package com.skungee.shared;

public interface PlatformConfiguration {

	public int getConfigurationVersion();

	public String getBindAddress();

	public int getBufferSize();

	public boolean isDebug();

	public int getPort();

}
