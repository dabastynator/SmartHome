package de.remote.gpiopower.api;

import de.newsystem.rmi.protokol.RemoteAble;
import de.newsystem.rmi.protokol.RemoteException;

/**
 * The GPIO-Power interface provides functionality to control power points with
 * the gpio pins of the raspberry pi.
 * 
 * @author sebastian
 */
public interface IGPIOPower extends RemoteAble {

	/**
	 * Remote id of the object
	 */
	public static final String ID = "de.remote.GPIOPower";
		
	/**
	 * default port of the server 
	 */
	public static final int PORT = 5034;
	
	public enum Switch {
		SWITCH_A, SWITCH_B, SWITCH_C, SWITCH_D
	}

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
	public void setState(State state, Switch powerSwitch)
			throws RemoteException;

}
