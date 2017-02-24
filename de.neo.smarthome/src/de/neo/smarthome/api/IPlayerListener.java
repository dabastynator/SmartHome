package de.neo.smarthome.api;

import de.neo.remote.rmi.Oneway;
import de.neo.remote.rmi.RemoteAble;
import de.neo.remote.rmi.RemoteException;

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
