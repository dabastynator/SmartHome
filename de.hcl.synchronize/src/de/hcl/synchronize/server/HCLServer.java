package de.hcl.synchronize.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import de.hcl.synchronize.api.IHCLClient;
import de.hcl.synchronize.api.IHCLServer;

/**
 * the implementation of the server interface holds the list of clients.
 * 
 * @author sebastian
 */
public class HCLServer extends Thread implements IHCLServer {

	/**
	 * list of all clients
	 */
	private List<IHCLClient> clients = new ArrayList<IHCLClient>();

	@Override
	public IHCLClient getClient(int index) throws RemoteException {
		return clients.get(index);
	}

	@Override
	public boolean addClient(IHCLClient client) throws RemoteException {
		return clients.add(client);
	}

	@Override
	public boolean removeClient(IHCLClient client) throws RemoteException {
		return clients.remove(client);
	}

	@Override
	public int getClientSize() throws RemoteException {
		return clients.size();
	}

}
