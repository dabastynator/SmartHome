package de.neo.remote.api;

import de.neo.rmi.api.Oneway;
import de.neo.rmi.protokol.RemoteAble;
import de.neo.rmi.protokol.RemoteException;

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
	@Oneway
	public void playerMessage(PlayingBean playing) throws RemoteException;
	
}
