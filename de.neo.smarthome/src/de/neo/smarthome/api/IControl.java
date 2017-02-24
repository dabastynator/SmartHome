package de.neo.smarthome.api;

import java.io.IOException;

import de.neo.remote.protokol.RemoteAble;
import de.neo.remote.protokol.RemoteException;
import de.neo.remote.protokol.ServerPort;

/**
 * control the computer
 * 
 * @author sebastian
 */
public interface IControl extends RemoteAble {

	/**
	 * Code for left Click
	 */
	public static final int LEFT_CLICK = 0;

	/**
	 * Code for right Click
	 */
	public static final int RIGHT_CLICK = 1;

	/**
	 * switch the display off
	 * 
	 * @throws RemoteException
	 */
	public void displayDark() throws RemoteException;

	/**
	 * switch the display on
	 * 
	 * @throws RemoteException
	 */
	public void displayBride() throws RemoteException;

	/**
	 * shutdown the computer
	 * 
	 * @throws RemoteException
	 */
	public void shutdown() throws RemoteException;

	/**
	 * Move the mouse cursor to specified position.
	 * 
	 * @param x
	 * @param y
	 * @throws RemoteException
	 */
	public void mouseMove(int x, int y) throws RemoteException;

	/**
	 * Create a stream for mouse move actions.
	 * 
	 * @return port of the socket with the stream
	 * @throws RemoteException, IOException
	 */
	public ServerPort openMouseMoveStream() throws RemoteException, IOException;

	/**
	 * Performe specified mouse action.
	 * 
	 * @param button
	 * @throws RemoteException
	 */
	public void mousePress(int button) throws RemoteException;

	/**
	 * Press specified key.
	 * 
	 * @param charachters
	 * @throws RemoteException
	 */
	public void keyPress(String charachters) throws RemoteException;

}
