package de.neo.remote.gpio;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import de.neo.remote.RemoteLogger;
import de.neo.remote.api.IInternetSwitch.State;
import de.neo.rmi.api.RMILogger.LogPriority;

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
			Process sender = Runtime
					.getRuntime()
					.exec(new String[] { SWITCH_SENDER, familyCode,
							switchNumber + "", (state == State.ON) ? "1" : "0" });
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					sender.getErrorStream()));
			String line = null;
			boolean success = true;
			while ((line = reader.readLine()) != null) {
				RemoteLogger.performLog(LogPriority.ERROR, line,
						"Internetswitch");
				success = false;
			}
			if (success)
				RemoteLogger.performLog(LogPriority.INFORMATION, "Set switch "
						+ familyCode + " " + switchNumber + " to " + state,
						"Internetswitch");
			reader.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getSimpleName() + ": "
					+ e.getMessage());
		}
	}

}
