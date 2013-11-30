package de.neo.remote.gpiopower;

import de.neo.remote.gpiopower.api.IInternetSwitch.State;

/**
 * Implements the gpio power by executing switch commands
 * 
 * @author sebastian
 * 
 */
public class SwitchPower {

	private static final String SWITCH_SENDER = "/home/troubadix/rcswitch-pi/send";

	public synchronized void setSwitchState(String familyCode,
			int switchNumber, State state) {
		try {
			Process sender = Runtime
					.getRuntime()
					.exec(new String[] { SWITCH_SENDER, familyCode,
							switchNumber + "", (state == State.ON) ? "1" : "0" });
			sender.waitFor();
		} catch (Exception e) {
			System.err.println(e.getClass().getSimpleName() + ": "
					+ e.getMessage());
		}
		System.out.println("Set switch " + familyCode + " " + switchNumber
				+ " to " + state);
	}

}
