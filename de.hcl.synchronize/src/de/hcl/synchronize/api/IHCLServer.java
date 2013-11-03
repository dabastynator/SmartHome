package de.hcl.synchronize.api;

import de.neo.rmi.protokol.RemoteAble;
import de.neo.rmi.protokol.RemoteException;

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
	 * Get session object by id.
	 * 
	 * @param sessionID
	 * @return Session object
	 * @throws RemoteException
	 */
	public IHCLSession getSession(String sessionID) throws RemoteException;
}
