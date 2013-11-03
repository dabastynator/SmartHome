package de.neo.rmi.handler;

import de.neo.rmi.api.RMILogger;
import de.neo.rmi.api.Server;
import de.neo.rmi.api.RMILogger.LogPriority;

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
