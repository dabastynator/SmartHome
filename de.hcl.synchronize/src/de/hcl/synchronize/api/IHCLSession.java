package de.hcl.synchronize.api;

import de.hcl.synchronize.api.IHCLClient.FileBean;
import de.neo.rmi.api.Oneway;
import de.neo.rmi.protokol.RemoteAble;
import de.neo.rmi.protokol.RemoteException;

/**
 * The session holds a list of clients to be synchronized.
 * 
 * @author sebastian
 * 
 */
public interface IHCLSession extends RemoteAble {

	/**
	 * Get the session id.
	 * 
	 * @return Session id
	 * @throws RemoteException
	 */
	public String getSessionID() throws RemoteException;

	/**
	 * Get remoteable home cloud client at index in synchronization session.
	 * 
	 * @param client
	 *            index of the client in the map
	 * @return list of cloud clients
	 * @throws RemoteException
	 */
	public IHCLClient getClient(int index) throws RemoteException;

	/**
	 * Get count of all clients in synchronization session.
	 * 
	 * @return size of clients
	 * @throws RemoteException
	 */
	public int getClientSize() throws RemoteException;

	/**
	 * Add new client to client list in synchronization session.
	 * 
	 * @param client
	 * @return true for success
	 * @throws RemoteException
	 */
	public boolean addClient(IHCLClient client) throws RemoteException;

	/**
	 * Remove client from client list in synchronization session.
	 * 
	 * @param client
	 * @return true for success
	 * @throws RemoteException
	 */
	public boolean removeClient(IHCLClient client) throws RemoteException;

	/**
	 * Inform session about new file from client.
	 * 
	 * @param client
	 * @param bean
	 * @throws RemoteException
	 */
	@Oneway
	public void createFile(IHCLClient client, FileBean bean)
			throws RemoteException;

	/**
	 * Inform session about deleted file from client.
	 * 
	 * @param client
	 * @param bean
	 * @throws RemoteException
	 */
	@Oneway
	public void deleteFile(IHCLClient client, FileBean bean)
			throws RemoteException;

	/**
	 * Inform session about renamed file from client.
	 * 
	 * @param client
	 * @param subfolder
	 * @param oldName
	 * @param newName
	 * @throws RemoteException
	 */
	@Oneway
	public void renameFile(IHCLClient client, String subfolder, String oldName,
			String newName) throws RemoteException;

	/**
	 * Inform session about modified file from client.
	 * 
	 * @param client
	 * @param bean
	 * @throws RemoteException
	 */
	@Oneway
	public void modifiedFile(IHCLClient client, FileBean bean)
			throws RemoteException;
}
