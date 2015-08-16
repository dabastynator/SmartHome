package de.neo.remote.api;

import de.neo.rmi.protokol.RemoteAble;
import de.neo.rmi.protokol.RemoteException;

/**
 * The IRCColor interface specifies the api call to set a color for a remote
 * able object. The color code will be transmitted by the gpios and a 433 MHz
 * sender.
 * 
 * @author sebastian
 *
 */
public interface IRCColor extends RemoteAble {

	/**
	 * Set the specified color for the object.
	 * 
	 * @param color
	 * @throws RemoteException
	 */
	public void setColor(int color) throws RemoteException;

	/**
	 * Set the specified color for the object, just for specified duration.
	 * 
	 * @param color
	 * @param duration
	 *            in ms
	 * @throws RemoteException
	 */
	public void setColor(int color, int duration) throws RemoteException;

	/**
	 * Get current color of the color object, temporary color will not returned.
	 * 
	 * @return current color
	 * @throws RemoteException
	 */
	public int getColor() throws RemoteException;

}
