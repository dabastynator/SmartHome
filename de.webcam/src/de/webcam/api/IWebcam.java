package de.webcam.api;

import de.newsystem.rmi.protokol.RemoteException;

/**
 * The webcam provides functionality to capture pictures of a webcam.
 * 
 * @author sebastian
 */
public interface IWebcam {

	/**
	 * port of the webcam server
	 */
	public static final int PORT = 5033;

	/**
	 * id of the webcam server
	 */
	public static final String WEBCAM_SERVER = "de.remote.webcamserver";

	/**
	 * add observer for the webcam. the observer gets pictures of the webcam.
	 * 
	 * @param listener
	 * @throws RemoteException
	 */
	public void addWebcamListener(IWebcamListener listener)
			throws RemoteException;

	/**
	 * add observer for the webcam. the observer gets pictures of the webcam.
	 * the frame will be compressed to given dimension.
	 * 
	 * @param listener
	 * @throws RemoteException
	 */
	public void addWebcamListener(IWebcamListener listener, int width,
			int height) throws RemoteException;

	/**
	 * remove observer for the webcam.
	 * 
	 * @param listener
	 * @throws RemoteException
	 */
	public void removeWebcamListener(IWebcamListener listener)
			throws RemoteException;

}
