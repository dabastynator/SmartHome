package de.remote.mediaserver.api;

import de.newsystem.rmi.api.Oneway;
import de.newsystem.rmi.protokol.RemoteAble;
import de.newsystem.rmi.protokol.RemoteException;

/**
 * the listener informs about a new message
 * 
 * @author sebastian
 */
public interface IChatListener extends RemoteAble {

	/**
	 * inform about a new message. the client is the author of the message, time
	 * contains the time when the message comes to the server.
	 * 
	 * @param client
	 * @param msg
	 * @param time
	 * @throws RemoteException
	 */
	@Oneway
	public void informMessage(String client, String message, String time)
			throws RemoteException;

	/**
	 * inform about new client
	 * 
	 * @param client
	 * @throws RemoteException
	 */
	@Oneway
	public void informNewClient(String client) throws RemoteException;

	/**
	 * inform about client that has left the chat room
	 * 
	 * @param client
	 * @throws RemoteException
	 */
	@Oneway
	public void informLeftClient(String client) throws RemoteException;

	/**
	 * get the name of the client
	 * 
	 * @return name
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException;
}
