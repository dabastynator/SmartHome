package de.neo.smarthome.mobile.api;

/**
 * exception for music player
 * @author sebastian
 */
public class PlayerException extends Exception{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3782374046548196941L;

	/**
	 * allocate new exception
	 * @param message
	 */
	public PlayerException(String message) {
		super(message);
	}

}
