package de.remote.gpiopower;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.gpiopower.api.IGPIOListener;
import de.remote.gpiopower.api.IGPIOPower;

/**
 * Implements the gpio power by executing gpio commands
 * 
 * @author sebastian
 */
public class GPIOPower implements IGPIOPower {

	// match gpio numbers in constans
	private static final int PIN_ON = 24;
	private static final int PIN_OFF = 23;

	private static final int PIN_A = 14;
	private static final int PIN_B = 15;
	private static final int PIN_C = 18;
	private static final int PIN_D = 25;

	// match constants in state and switch map
	private static final Map<Switch, Integer> switches = new HashMap<Switch, Integer>();
	private static final Map<State, Integer> states = new HashMap<State, Integer>();

	private static final Map<Switch, State> actualState = new HashMap<IGPIOPower.Switch, IGPIOPower.State>();

	static {
		switches.put(Switch.A, PIN_A);
		switches.put(Switch.B, PIN_B);
		switches.put(Switch.C, PIN_C);
		switches.put(Switch.D, PIN_D);

		states.put(State.ON, PIN_ON);
		states.put(State.OFF, PIN_OFF);

		actualState.put(Switch.A, State.OFF);
		actualState.put(Switch.B, State.OFF);
		actualState.put(Switch.C, State.OFF);
		actualState.put(Switch.D, State.OFF);
	}

	/**
	 * singelton object
	 */
	private static GPIOPower singelton;

	/**
	 * Listener for power switch change.
	 */
	private List<IGPIOListener> listeners = Collections
			.synchronizedList(new ArrayList<IGPIOListener>());

	/**
	 * The gpio power is a singelton.
	 */
	private GPIOPower() {
		System.out.println("initialize gpio pins, set as output and write 0");
		setAsOutput(PIN_ON);
		setAsOutput(PIN_OFF);
		setAsOutput(PIN_A);
		setAsOutput(PIN_B);
		setAsOutput(PIN_C);
		setAsOutput(PIN_D);
		writeGPIO(PIN_ON, 0);
		writeGPIO(PIN_OFF, 0);
		writeGPIO(PIN_A, 0);
		writeGPIO(PIN_B, 0);
		writeGPIO(PIN_C, 0);
		writeGPIO(PIN_D, 0);
	}

	/**
	 * @return static singelton gpio power
	 */
	public static GPIOPower getGPIOPower() {
		if (singelton == null)
			singelton = new GPIOPower();
		return singelton;
	}

	/**
	 * Write specified value on specified gpio pin.
	 * 
	 * @param pin
	 * @param value
	 */
	private synchronized void writeGPIO(int pin, int value) {
		try {
			Runtime.getRuntime()
					.exec(new String[] { "gpio", "-g", "write", pin + "",
							value + "" });
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Set specified pin as output.
	 * 
	 * @param pin
	 */
	private void setAsOutput(int pin) {
		try {
			Runtime.getRuntime().exec(
					new String[] { "gpio", "-g", "mode", pin + "", "out" });
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	@Override
	public synchronized void setState(final State state,
			final Switch powerSwitch) throws RemoteException {
		writeGPIO(states.get(state), 1);
		writeGPIO(switches.get(powerSwitch), 1);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writeGPIO(states.get(state), 0);
		writeGPIO(switches.get(powerSwitch), 0);

		actualState.put(powerSwitch, state);
		System.out.println("Set switch " + powerSwitch + " to " + state);
		new Thread() {
			public void run() {
				informListener(state, powerSwitch);
			};
		}.start();

	}

	private void informListener(State state, Switch powerSwitch) {
		List<IGPIOListener> exceptionList = new ArrayList<IGPIOListener>();
		for (IGPIOListener listener : listeners) {
			try {
				listener.onPowerSwitchChange(powerSwitch, state);
			} catch (RemoteException e) {
				exceptionList.add(listener);
			}
		}
		listeners.removeAll(exceptionList);
	}

	@Override
	public State getState(Switch powerSwitch) throws RemoteException {
		return actualState.get(powerSwitch);
	}

	@Override
	public void registerPowerSwitchListener(IGPIOListener listener)
			throws RemoteException {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	@Override
	public void unregisterPowerSwitchListener(IGPIOListener listener)
			throws RemoteException {
		listeners.remove(listener);
	}

}
