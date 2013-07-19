package de.remote.gpiopower.api;

import de.newsystem.rmi.protokol.RemoteAble;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.gpiopower.api.IInternetSwitch.State;

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
