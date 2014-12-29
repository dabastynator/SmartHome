package de.neo.remote.gpio;

import java.util.BitSet;

import com.pi4j.io.gpio.RaspiPin;

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

	private RCSwitch mRcSwitch;

	public SwitchPower() {
		mRcSwitch = new RCSwitch(RaspiPin.GPIO_17);
	}

	public synchronized void setSwitchState(String familyCode,
			int switchNumber, State state) {
		BitSet bitSet = new BitSet(5);
		for (int i = 0; i < 5; i++)
			if (familyCode.charAt(i) == 1)
				bitSet.set(i);
		if (state == State.ON)
			mRcSwitch.switchOn(bitSet, switchNumber);
		else
			mRcSwitch.switchOff(bitSet, switchNumber);
		RemoteLogger.performLog(LogPriority.INFORMATION, "Set switch "
				+ familyCode + " " + switchNumber + " to " + state,
				"Internetswitch");
	}

}
