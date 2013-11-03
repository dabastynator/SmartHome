package de.neo.remote.gpiopower.api;

import de.neo.remote.gpiopower.api.IInternetSwitch.State;
import de.neo.rmi.protokol.RemoteAble;
import de.neo.rmi.protokol.RemoteException;

/**
 * The IGPIOListener listens for new states of gpio power object.
 * 
 * @author sebastian
 */
public interface IInternetSwitchListener extends RemoteAble {

	/**
	 * Call on change of power switch.
	 * 
	 * @param _switch
	 * @param state
	 * @throws RemoteException
	 */
	public void onPowerSwitchChange(String switchName, State state) throws RemoteException;

}
