package com.skungee.proxy;

import com.skungee.shared.PlatformConfiguration;

public interface ProxyConfiguration extends PlatformConfiguration {

	public boolean hasBackupConsoleMessages();

	public String getVariableDatabaseType();

	public long getMinutesBackupInterval();

	public boolean isBackupsEnabled();

}
