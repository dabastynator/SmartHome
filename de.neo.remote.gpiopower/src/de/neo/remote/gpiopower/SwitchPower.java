package de.neo.remote.gpiopower;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import de.neo.remote.gpiopower.api.IInternetSwitch.State;

/**
 * Implements the gpio power by executing switch commands
 * 
 * @author sebastian
 * 
 */
public class SwitchPower {

	private static final String SWITCH_SENDER = "/usr/bin/send";

	public synchronized void setSwitchState(String familyCode,
			int switchNumber, State state) {
		try {
			System.out.println("Set switch " + familyCode + " " + switchNumber
					+ " to " + state);
			Process sender = Runtime
					.getRuntime()
					.exec(new String[] { SWITCH_SENDER, familyCode,
							switchNumber + "", (state == State.ON) ? "1" : "0" });
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					sender.getErrorStream()));
			String line = null;
			while ((line = reader.readLine()) != null)
				System.out.println("Error: " + line);
			reader.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getSimpleName() + ": "
					+ e.getMessage());
		}
	}

}
