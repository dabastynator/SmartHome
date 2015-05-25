package de.neo.remote.api;

import java.io.IOException;

import de.neo.rmi.protokol.RemoteAble;
import de.neo.rmi.protokol.RemoteException;

/**
 * The Command-Action represents a action to execute remoteable. The action
 * executes a simple command line with parameter.
 * 
 * @author sebastian
 *
 */
public interface ICommandAction extends RemoteAble {

	/**
	 * Start the action. Throws io exception, if error occur on executing
	 * 
	 * @throws RemoteException
	 * @throws IOException
	 */
	public void startAction() throws RemoteException, IOException;

	/**
	 * Stop current action.
	 * 
	 * @throws RemoteException
	 */
	public void stopAction() throws RemoteException;

	/**
	 * Get thumbnail for the action.
	 * 
	 * @return action thumbnail
	 * @throws RemoteException
	 */
	public int[] getThumbnail() throws RemoteException;

	/**
	 * Get thumbnail width for the action.
	 * 
	 * @return image width
	 * @throws RemoteException
	 */
	public int getThumbnailWidth() throws RemoteException;

	/**
	 * Get thumbnail height for the action.
	 * 
	 * @return image height
	 * @throws RemoteException
	 */
	public int getThumbnailHeight() throws RemoteException;

	/**
	 * @return true if action is running, false otherwise.
	 * @throws RemoteException
	 */
	public boolean isRunning() throws RemoteException;

	/**
	 * Get a client action, if there is any one.
	 * 
	 * @return client action
	 * @throws RemoteException
	 */
	public String getClientAction() throws RemoteException;
}
