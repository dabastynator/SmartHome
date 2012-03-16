package de.remote.api;

import de.newsystem.rmi.api.oneway;
import de.newsystem.rmi.protokol.RemoteAble;
import de.newsystem.rmi.protokol.RemoteException;

/**
 * the listener informs about a new message
 * 
 * @author sebastian
 */
public interface IChatListener extends RemoteAble {

	/**
	 * inform about a new message by author
	 * 
	 * @param client
	 * @param msg
	 * @throws RemoteException
	 */
	@oneway
	public void informMessage(String client, String msg) throws RemoteException;

	/**
	 * inform about new client
	 * 
	 * @param client
	 * @throws RemoteException
	 */
	@oneway
	public void informNewClient(String client) throws RemoteException;

	/**
	 * inform about client that has left the chat room
	 * 
	 * @param client
	 * @throws RemoteException
	 */
	@oneway
	public void informLeftClient(String client) throws RemoteException;

	/**
	 * get the name of the client
	 * 
	 * @return name
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException;
}
