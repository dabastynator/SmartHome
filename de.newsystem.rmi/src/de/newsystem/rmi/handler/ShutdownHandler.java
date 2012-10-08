package de.newsystem.rmi.handler;

import de.newsystem.rmi.api.RMILogger;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.api.RMILogger.LogPriority;

/**
 * The shutdown handler performs shutdown of the server. In unregisters
 * registered global objects.
 * 
 * @author sebastian
 */
public class ShutdownHandler extends Thread {

	/**
	 * The server to shutdown
	 */
	private Server server;

	public ShutdownHandler(Server server) {
		this.server = server;
	}

	@Override
	public void run() {
		RMILogger.performLog(LogPriority.WARNING, "Server shutdown", "");
		for (String id : server.getRegisteredIDs().toArray(new String[] {}))
			server.unRegister(id);
	}

}
