package de.neo.remote.mediaserver.api;

import de.neo.rmi.protokol.RemoteAble;
import de.neo.rmi.protokol.RemoteException;

/**
 * The ImageViewer provides functionality to show images and dia-shows.
 * 
 * @author sebastian
 */
public interface IImageViewer extends RemoteAble {

	/**
	 * show given image-file
	 * 
	 * @param file
	 * @throws RemoteException
	 */
	void show(String file) throws RemoteException, ImageException;

	/**
	 * exit the image show.
	 * 
	 * @throws RemoteException
	 */
	void quit() throws RemoteException, ImageException;

	/**
	 * @param imageTime
	 *            The time one image is on the screen.
	 * @throws RemoteException
	 */
	void toggleDiashow(int imageTime) throws RemoteException, ImageException;

	/**
	 * show next image in the folder.
	 * 
	 * @throws RemoteException
	 */
	void next() throws RemoteException, ImageException;

	/**
	 * show previous image in the folder.
	 * 
	 * @throws RemoteException
	 */
	void previous() throws RemoteException, ImageException;

}
