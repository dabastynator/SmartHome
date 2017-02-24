package de.neo.smarthome.api;

import de.neo.remote.protokol.RemoteAble;
import de.neo.remote.protokol.RemoteException;
import de.neo.smarthome.api.IWebSwitch.State;

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
	public void onPowerSwitchChange(String switchId, State state) throws RemoteException;

}
