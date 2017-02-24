package de.neo.smarthome.api;

import de.neo.remote.api.Oneway;
import de.neo.remote.protokol.RemoteAble;
import de.neo.remote.protokol.RemoteException;

/**
 * The thumbnail listener gets thumbnail for entities.
 * 
 * @author sebastian
 */
public interface IThumbnailListener extends RemoteAble {

	/**
	 * Set the thumbnail for specified entity, with width and height.
	 * 
	 * @param file
	 * @param width
	 * @param height
	 * @param thumbnail
	 * @throws RemoteException
	 */
	@Oneway
	public void setThumbnail(String file, int width, int height, int[] thumbnail)
			throws RemoteException;

}
