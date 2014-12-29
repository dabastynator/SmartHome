package de.neo.remote.gpio;

import java.util.BitSet;

import com.pi4j.io.gpio.RaspiPin;

import de.neo.remote.api.IInternetSwitch.State;

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
		BitSet bitSet = RCSwitch.getSwitchGroupAddress(familyCode);
		if (state == State.ON)
			mRcSwitch.switchOn(bitSet, switchNumber);
		else
			mRcSwitch.switchOff(bitSet, switchNumber);
	}

}
