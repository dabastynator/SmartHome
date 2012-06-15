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
	 * Each pixel is stored on 2 bytes and only the RGB channels are encoded:
	 * red is stored with 5 bits of precision (32 possible values), green is
	 * stored with 6 bits of precision (64 possible values) and blue is stored
	 * with 5 bits of precision. This configuration can produce slight visual
	 * artifacts depending on the configuration of the source. For instance,
	 * without dithering, the result might show a greenish tint. To get better
	 * results dithering should be applied. This configuration may be useful
	 * when using opaque bitmaps that do not require high color fidelity.
	 */
	public static final int RGB_565 = 1;

	/**
	 * Each pixel is stored on 4 bytes.
	 */
	public static final int RGB_8888 = 2;

	/**
	 * start capturing, if webcam is already not capturing, otherwise the method
	 * does nothing. if capturing fails, webcam exception will be thrown.
	 * 
	 * @throws RemoteException
	 * @throws WebcamException
	 */
	public void startCapture() throws RemoteException, WebcamException;
	
	/**
	 * stop capturing, if webcam is already capturing, otherwise the method
	 * does nothing. if stop capturing fails, webcam exception will be thrown.
	 * 
	 * @throws RemoteException
	 * @throws WebcamException
	 */
	public void stopCapture() throws RemoteException, WebcamException;

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
	 * add observer for the webcam. the observer gets pictures of the webcam.
	 * the frame will be compressed to given dimension and quality type.
	 * 
	 * @param listener
	 * @throws RemoteException
	 */
	public void addWebcamListener(IWebcamListener listener, int width,
			int height, int quality) throws RemoteException;

	/**
	 * remove observer for the webcam.
	 * 
	 * @param listener
	 * @throws RemoteException
	 */
	public void removeWebcamListener(IWebcamListener listener)
			throws RemoteException;

}
