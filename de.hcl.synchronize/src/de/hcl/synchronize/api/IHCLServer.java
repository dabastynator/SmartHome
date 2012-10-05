package de.hcl.synchronize.api;

import de.newsystem.rmi.protokol.RemoteAble;
import de.newsystem.rmi.protokol.RemoteException;

/**
 * The home cloud server holds a list of synchronization sessions.
 * 
 * @author sebastian
 * 
 */
public interface IHCLServer extends RemoteAble {

	/**
	 * Id of the server object in the registry
	 */
	public static final String SERVER_ID = "de.hcl.server";
	
	/**
	 * Port of the synchronizing server
	 */
	public static final int SERVER_PORT = 5056;

	/**
	 * Get all session ids
	 * 
	 * @return array of session ids
	 * @throws RemoteException
	 */
	public String[] getSessionIDs() throws RemoteException;

	/**
	 * Get remoteable home cloud client at index in synchronization session.
	 * 
	 * @param synchronizationID
	 *            the id determines the id of several synchronization sessions
	 * @param client
	 *            index of the client in the map
	 * @return list of cloud clients
	 * @throws RemoteException
	 */
	public IHCLClient getClient(String synchronizationID, int index)
			throws RemoteException;

	/**
	 * Get count of all clients in synchronization session.
	 * 
	 * @return size of clients
	 * @throws RemoteException
	 */
	public int getClientSize(String synchronizationID) throws RemoteException;

	/**
	 * Add new client to client list in synchronization session.
	 * 
	 * @param client
	 * @return true for success
	 * @throws RemoteException
	 */
	public boolean addClient(String synchronizationID, IHCLClient client)
			throws RemoteException;

	/**
	 * Remove client from client list in synchronization session.
	 * 
	 * @param client
	 * @return true for success
	 * @throws RemoteException
	 */
	public boolean removeClient(String synchronizationID, IHCLClient client)
			throws RemoteException;

}
