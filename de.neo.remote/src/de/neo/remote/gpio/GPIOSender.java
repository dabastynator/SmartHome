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
public class GPIOSender {

	private static final String SWITCH_SENDER = "/usr/bin/send";

	private static final String COLOR_SENDER = "/usr/bin/sendInt";

	private static GPIOSender mInstance;

	private GPIOSender() {
	}

	public static GPIOSender getInstance() {
		if (mInstance == null)
			mInstance = new GPIOSender();
		return mInstance;
	}

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
			RemoteLogger.performLog(LogPriority.ERROR, e.getMessage(),
					"SwitchPower");
		}
	}

	public synchronized void setColor(int color) {
		try {
			Process sender = Runtime.getRuntime().exec(
					new String[] { COLOR_SENDER,
							String.valueOf(0X0A000000 | color) });
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
				RemoteLogger.performLog(LogPriority.INFORMATION, "Set color "
						+ color, "RCColor");
			reader.close();
		} catch (Exception e) {
			RemoteLogger.performLog(LogPriority.ERROR, e.getMessage(),
					"SwitchPower");
		}
	}

}
