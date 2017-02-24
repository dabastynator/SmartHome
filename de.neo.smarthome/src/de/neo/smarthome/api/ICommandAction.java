package de.neo.smarthome.api;

import java.io.IOException;

import de.neo.remote.protokol.RemoteAble;
import de.neo.remote.protokol.RemoteException;

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
	 * Get icon for the action. The image is a base64 encoded png image.
	 * 
	 * @return action icon
	 * @throws RemoteException
	 */
	public String getIconBase64() throws RemoteException;

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
