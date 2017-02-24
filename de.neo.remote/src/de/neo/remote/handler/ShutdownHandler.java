package de.neo.remote.handler;

import de.neo.remote.api.RMILogger;
import de.neo.remote.api.RMILogger.LogPriority;
import de.neo.remote.api.Server;

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
