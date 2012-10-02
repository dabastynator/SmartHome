package de.hcl.synchronize.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hcl.synchronize.api.IHCLClient;
import de.hcl.synchronize.api.IHCLServer;
import de.hcl.synchronize.log.HCLLogger;
import de.hcl.synchronize.log.IHCLLogListener.HCLType;

/**
 * the implementation of the server interface holds the list of clients.
 * 
 * @author sebastian
 */
public class HCLServer extends Thread implements IHCLServer {

	/**
	 * list of all clients
	 */
	private Map<String, List<IHCLClient>> sessions = new HashMap<String, List<IHCLClient>>();

	@Override
	public String[] getSessionIDs() throws RemoteException {
		return sessions.keySet().toArray(new String[] {});
	}

	@Override
	public IHCLClient getClient(String synchronizationID, int index)
			throws RemoteException {
		return sessions.get(synchronizationID).get(index);
	}

	@Override
	public boolean addClient(String synchronizationID, IHCLClient client)
			throws RemoteException {
		if (!sessions.containsKey(synchronizationID)) {
			sessions.put(synchronizationID, new ArrayList<IHCLClient>());
			HCLLogger.performLog("create new session: " + synchronizationID, HCLType.CREATE, this);
		}
		return sessions.get(synchronizationID).add(client);
	}

	@Override
	public boolean removeClient(String synchronizationID, IHCLClient client)
			throws RemoteException {
		return sessions.get(synchronizationID).remove(client);
	}

	@Override
	public int getClientSize(String synchronizationID) throws RemoteException {
		return sessions.get(synchronizationID).size();
	}

}
