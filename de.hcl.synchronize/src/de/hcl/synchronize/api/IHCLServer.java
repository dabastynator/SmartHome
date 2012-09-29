package de.hcl.synchronize.api;

import java.rmi.RemoteException;

import de.newsystem.rmi.protokol.RemoteAble;

/**
 * The home cloud server holds a list of cloud clients.
 * 
 * @author sebastian
 * 
 */
public interface IHCLServer extends RemoteAble {

	/**
	 * id of the server object in the registry
	 */
	public static final String SERVER_ID = "de.hcl.server";

	/**
	 * get remoteable home cloud client at index.
	 * 
	 * @param client
	 *            index
	 * @return list of cloud clients
	 * @throws RemoteException
	 */
	public IHCLClient getClient(int index) throws RemoteException;

	/**
	 * get count of all clients.
	 * 
	 * @return size of clients
	 * @throws RemoteException
	 */
	public int getClientSize() throws RemoteException;

	/**
	 * add new client to client list
	 * 
	 * @param client
	 * @return true for success
	 * @throws RemoteException
	 */
	public boolean addClient(IHCLClient client) throws RemoteException;

	/**
	 * remove client from client list
	 * 
	 * @param client
	 * @return true for success
	 * @throws RemoteException
	 */
	public boolean removeClient(IHCLClient client) throws RemoteException;

}
