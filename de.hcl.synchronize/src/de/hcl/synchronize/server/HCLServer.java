package de.hcl.synchronize.server;

import java.util.HashMap;
import java.util.Map;

import de.hcl.synchronize.api.IHCLServer;
import de.hcl.synchronize.api.IHCLSession;
import de.neo.rmi.protokol.RemoteException;

/**
 * the implementation of the server interface holds the list of clients.
 * 
 * @author sebastian
 */
public class HCLServer extends Thread implements IHCLServer {

	/**
	 * list of all sessions
	 */
	private Map<String, IHCLSession> sessions = new HashMap<String, IHCLSession>();

	/**
	 * The queue executes jobs.
	 */
	private JobQueue queue;

	public HCLServer(JobQueue queue) {
		this.queue = queue;
	}

	@Override
	public String[] getSessionIDs() throws RemoteException {
		return sessions.keySet().toArray(new String[] {});
	}

	@Override
	public IHCLSession getSession(String sessionID) throws RemoteException {
		if (!sessions.containsKey(sessionID))
			sessions.put(sessionID, new HCLSession(sessionID, queue));
		return sessions.get(sessionID);
	}

}
