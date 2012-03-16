package de.remote.api;

import de.newsystem.rmi.protokol.RemoteAble;
import de.newsystem.rmi.protokol.RemoteException;

/**
 * control the computer
 * 
 * @author sebastian
 */
public interface IControl  extends RemoteAble{

	/**
	 * switch the display off
	 * @throws RemoteException
	 */
	public void displayDark() throws RemoteException;

	/**
	 * switch the display on
	 * @throws RemoteException
	 */
	public void displayBride() throws RemoteException;

	/**
	 * shutdown the computer
	 * @throws RemoteException
	 */
	public void shutdown() throws RemoteException;

}
