package de.neo.remote.gpiopower;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.neo.remote.gpiopower.api.IInternetSwitch;
import de.neo.remote.gpiopower.api.IInternetSwitchListener;
import de.neo.rmi.protokol.RemoteException;

public class InternetSwitchImpl implements IInternetSwitch {

	public static final String FAMILY_REGEX = "[01]{5}";

	/**
	 * Listener for power switch change.
	 */
	private List<IInternetSwitchListener> listeners = Collections
			.synchronizedList(new ArrayList<IInternetSwitchListener>());
	private String name;
	private SwitchPower power;
	private String type;
	private String familyCode;
	private int switchNumber;
	private State state;
	private float[] position;

	public InternetSwitchImpl(String name, SwitchPower power,
			String familyCode, int switchNumber, String type, float[] position) {
		if (!familyCode.matches(FAMILY_REGEX))
			throw new IllegalArgumentException("Invalid Familycode: "
					+ familyCode + ". must match " + FAMILY_REGEX);
		this.name = name;
		this.power = power;
		this.familyCode = familyCode;
		this.switchNumber = switchNumber;
		this.type = type;
		this.position = position;
		state = State.OFF;
	}

	@Override
	public void setState(final State state) throws RemoteException {
		power.setSwitchState(familyCode, switchNumber, state);
		this.state = state;
		new Thread() {
			public void run() {
				informListener(state);
			};
		}.start();
	}

	@Override
	public State getState() throws RemoteException {
		return state;
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
	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public float[] getPosition() {
		return position;
	}

}
