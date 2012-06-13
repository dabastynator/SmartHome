package de.webcam.api;

import de.newsystem.rmi.api.Oneway;
import de.newsystem.rmi.protokol.RemoteAble;
import de.newsystem.rmi.protokol.RemoteException;

/**
 * the webcam listener listens for new pictures of the webcam.
 * 
 * @author sebastian
 */
public interface IWebcamListener extends RemoteAble {

	/**
	 * new picture captured. the pixels are in the rgb array. one pixel consists
	 * of 0xffrrggbb.
	 * 
	 * @param width
	 * @param height
	 * @param rgb
	 * @throws RemoteException
	 */
	@Oneway
	public void onVideoFrame(int width, int height, int[] rgb)
			throws RemoteException;

}
