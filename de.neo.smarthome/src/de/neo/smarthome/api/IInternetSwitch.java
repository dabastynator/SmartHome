package de.neo.smarthome.api;

import de.neo.remote.rmi.RemoteAble;
import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.api.IWebSwitch.State;

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
	public void registerPowerSwitchListener(IInternetSwitchListener listener) throws RemoteException;

	/**
	 * Unregister power switch change listener.
	 * 
	 * @param listener
	 * @throws RemoteException
	 */
	public void unregisterPowerSwitchListener(IInternetSwitchListener listener) throws RemoteException;

	/**
	 * Get the type of the internet switch.
	 * 
	 * @return switch type
	 * @throws RemoteException
	 */
	public String getType() throws RemoteException;

	/**
	 * Get behavior of the switch. If the switch can just be read, but not set,
	 * the method returns true.
	 * 
	 * @return true if read only switch, false otherwise
	 * @throws RemoteException
	 */
	public boolean isReadOnly() throws RemoteException;
}
