package de.remote.api;

import de.newsystem.rmi.protokol.RemoteAble;
import de.newsystem.rmi.protokol.RemoteException;

/**
 * listener for current media file
 * @author sebastian
 */
public interface IPlayerListener extends RemoteAble{

	/**
	 * 
	 * @param playing
	 * @throws RemoteException
	 */
	public void playerMessage(PlayingBean playing) throws RemoteException;
	
}
