package com.skungee.spigot.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;

import com.skungee.shared.Skungee;

import ch.njol.skript.Skript;
import ch.njol.skript.util.Timespan;

public class Utils {

	public static boolean isEnum(Class<?> clazz, String object) {
		try {
			Method method = clazz.getMethod("valueOf", String.class);
			method.setAccessible(true);
			method.invoke(clazz, object.replace("\"", "").trim().replace(" ", "_").toUpperCase());
			return true;
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException error) {
			return false;
		}
	}

	/**
	 * Checks if the given String matches any of the other strings.
	 * 
	 * @param check The String to check against.
	 * @param options The matching options, that may be checked.
	 * @return boolean if the String matches any of the String options.
	 */
	public static boolean matchesIgnoreCase(String check, String... options) {
		for (String option : options) {
			if (check.equalsIgnoreCase(option))
				return true;
		}
		return false;
	}

	public static Class<?> getArrayClass(Class<?> parameter){
		return Array.newInstance(parameter, 0).getClass();
	}

	@SuppressWarnings("deprecation")
	public static int getTicks(Timespan time) {
		if (Skript.methodExists(Timespan.class, "getTicks_i")) {
			Number tick = time.getTicks_i();
			return tick.intValue();
		} else {
			return time.getTicks();
		}
	}

	public static void copyDirectory(File source, File destination) throws IOException {
		if (source.isDirectory()) {
			if (!destination.exists()) destination.mkdir();
			String files[] = source.list();
			for(int i = 0; i < files.length; i++) {
				copyDirectory(new File(source, files[i]), new File(destination, files[i]));
			}
		} else if (source.exists()) {
			InputStream in = new FileInputStream(source);
			OutputStream out = new FileOutputStream(destination);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}

	public static int findPort(int start, int max) {
		int port = start;
		Throwable lastException = null;
		while (port < max) {
			ServerSocket socket = null;
			try {
				socket = new ServerSocket(port);
				socket.setReuseAddress(true);
				return port;
			} catch (IOException e) {
				lastException = e;
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {}
				}
			}
			port++;
		}
		if (lastException != null)
			Skungee.getPlatform().consoleMessage("Could not find an open port between " + start + " and " + max + " make sure to port forward a port in this range.");
		return -1;
	}

}
