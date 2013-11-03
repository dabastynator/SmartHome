package de.neo.remote.mediaserver.api;

import de.newsystem.rmi.api.Oneway;
import de.newsystem.rmi.protokol.RemoteAble;
import de.newsystem.rmi.protokol.RemoteException;

/**
 * every chat client needs this
 * 
 * @author sebastian
 */
public interface IChatServer extends RemoteAble {

	/**
	 * id of the chat object
	 */
	public static final String ID = "de.newsystem.chat";
	
	/**
	 * post message, every listener will be informed
	 * 
	 * @param client
	 * @param msg
	 * @throws RemoteException
	 */
	@Oneway
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
