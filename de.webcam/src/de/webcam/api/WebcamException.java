package de.webcam.api;

/**
 * The webcam exception will be thrown if any exception with the webcam occurs.
 * 
 * @author sebastian
 */
public class WebcamException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * allocate new webcam exception with given description
	 * 
	 * @param msg
	 */
	public WebcamException(String msg) {
		super(msg);
	}

}
