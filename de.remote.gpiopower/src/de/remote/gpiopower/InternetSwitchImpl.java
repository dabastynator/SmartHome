package de.remote.gpiopower;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.gpiopower.GPIOPower.Switch;
import de.remote.gpiopower.api.IInternetSwitch;
import de.remote.gpiopower.api.IInternetSwitchListener;
import de.remote.gpiopower.api.IInternetSwitch.State;

public class InternetSwitchImpl implements IInternetSwitch {

	/**
	 * Listener for power switch change.
	 */
	private List<IInternetSwitchListener> listeners = Collections
			.synchronizedList(new ArrayList<IInternetSwitchListener>());
	private String name;
	private Switch _switch;
	private GPIOPower power;
	private String type;

	public InternetSwitchImpl(String name, GPIOPower power, Switch _switch, String type) {
		this.name = name;
		this.power = power;
		this._switch = _switch;
		this.type = type;
	}

	@Override
	public void setState(final State state) throws RemoteException {
		power.setState(state, _switch);
		new Thread() {
			public void run() {
				informListener(state);
			};
		}.start();
	}

	@Override
	public State getState() throws RemoteException {
		return power.getState(_switch);
	}

	@Override
	public void registerPowerSwitchListener(IInternetSwitchListener listener)
			throws RemoteException {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	@Override
	public void unregisterPowerSwitchListener(IInternetSwitchListener listener)
			throws RemoteException {
		listeners.remove(listener);
	}

	private void informListener(State state) {
		List<IInternetSwitchListener> exceptionList = new ArrayList<IInternetSwitchListener>();
		for (IInternetSwitchListener listener : listeners) {
			try {
				listener.onPowerSwitchChange(name, state);
			} catch (RemoteException e) {
				exceptionList.add(listener);
			}
		}
		listeners.removeAll(exceptionList);
	}
	
	@Override
	public String getType(){
		return type;
	}

}
