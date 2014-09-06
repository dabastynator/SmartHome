package de.neo.remote.api;

import de.neo.rmi.protokol.RemoteAble;
import de.neo.rmi.protokol.RemoteException;

/**
 * The GPIO-Power interface provides functionality to control power points with
 * the gpio pins of the raspberry pi.
 * 
 * @author sebastian
 */
public interface IInternetSwitch extends RemoteAble {

	/**
	 * default port of the server
	 */
	public static final int PORT = 5034;

	public enum State {
		ON, OFF
	}

	/**
	 * Set specified state on specified switch.
	 * 
	 * @param state
	 * @param powerSwitch
	 * @throws RemoteException
	 */
	public void setState(State state) throws RemoteException;

	/**
	 * @param powerSwitch
	 * @return state of the power switch
	 * @throws RemoteException
	 */
	public State getState() throws RemoteException;

	/**
	 * Register power switch change listener.
	 * 
	 * @param listener
	 * @throws RemoteException
	 */
	public void registerPowerSwitchListener(IInternetSwitchListener listener)
			throws RemoteException;

	/**
	 * Unregister power switch change listener.
	 * 
	 * @param listener
	 * @throws RemoteException
	 */
	public void unregisterPowerSwitchListener(IInternetSwitchListener listener)
			throws RemoteException;

	/**
	 * Get the type of the internet switch.
	 * 
	 * @return switch type
	 * @throws RemoteException
	 */
	public String getType() throws RemoteException;
}
