package de.neo.remote.rmi;

import de.neo.remote.rmi.RMILogger.LogPriority;

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
