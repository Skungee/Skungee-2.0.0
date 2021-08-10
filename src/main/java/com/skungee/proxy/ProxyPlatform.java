package com.skungee.proxy;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.Set;

import com.sitrica.japson.server.JapsonServer;
import com.sitrica.japson.shared.Handler;
import com.skungee.proxy.managers.EventManager;
import com.skungee.proxy.managers.ServerDataManager;
import com.skungee.proxy.variables.VariableManager;
import com.skungee.shared.Platform;
import com.skungee.shared.objects.SkungeeServer;

public interface ProxyPlatform extends Platform {

	/**
	 * API access.
	 * 
	 * @param handler The Japson handler to register to the proxy.
	 * @throws IllegalAccessException if the packet id doesn't match that of Packets.API
	 */
	public void setApiHandler(Handler handler) throws IllegalAccessException;

	public Optional<SkungeeServer> getServer(InetSocketAddress address);

	public Optional<SkungeeServer> getServer(String name);

	@Override
	public ProxyConfiguration getPlatformConfiguration();

	public ServerDataManager getServerDataManager();

	public VariableManager getVariableManager();

	public Set<SkungeeServer> getServers();

	public EventManager getEventManager();

	public JapsonServer getJapsonServer();

	public File getScriptsDirectory();

}
