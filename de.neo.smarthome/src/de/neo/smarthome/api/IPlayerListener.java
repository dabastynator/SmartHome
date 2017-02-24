package de.neo.smarthome.api;

import de.neo.remote.api.Oneway;
import de.neo.remote.protokol.RemoteAble;
import de.neo.remote.protokol.RemoteException;

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
