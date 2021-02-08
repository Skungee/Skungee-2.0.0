package com.skungee.proxy;

import java.net.InetAddress;
import java.util.Set;

import com.skungee.shared.PlatformConfiguration;

public interface ProxyConfiguration extends PlatformConfiguration {

	public Set<InetAddress> getWhitelistedAddresses();

	public boolean hasBackupConsoleMessages();

	public String getVariableDatabaseType();

	public long getMinutesBackupInterval();

	public boolean isBackupsEnabled();

}
