package de.remote.api;

import de.newsystem.rmi.api.oneway;
import de.newsystem.rmi.protokol.RemoteAble;
import de.newsystem.rmi.protokol.RemoteException;

/**
 * every chat client needs this
 * 
 * @author sebastian
 */
public interface IChatServer extends RemoteAble {

	/**
	 * post message, every listener will be informed
	 * 
	 * @param client
	 * @param msg
	 * @throws RemoteException
	 */
	@oneway
	public void postMessage(String client, String msg) throws RemoteException;

	/**
	 * add chat listener from server
	 * 
	 * @param listener
	 * @return add
	 * @throws RemoteException
	 */
	public boolean addChatListener(IChatListener listener)
			throws RemoteException;

	/**
	 * remove chat listener from server
	 * 
	 * @param listener
	 * @return remove
	 * @throws RemoteException
	 */
	public boolean removeChatListener(IChatListener listener)
			throws RemoteException;

	/**
	 * get array of all client names
	 * 
	 * @return clients
	 * @throws RemoteException
	 */
	public String[] getAllClients() throws RemoteException;
}
