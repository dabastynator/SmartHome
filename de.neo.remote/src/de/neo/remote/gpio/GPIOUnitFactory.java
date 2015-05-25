package de.neo.remote.gpio;

import de.neo.remote.AbstractControlUnit;
import de.neo.remote.ControlUnitFactory;

public class GPIOUnitFactory implements ControlUnitFactory {

	private SwitchPower mPower;

	public GPIOUnitFactory() {
		mPower = new SwitchPower();
	}

	@Override
	public AbstractControlUnit createControlUnit() {
		return new GPIOControlUnit(mPower);
	}

}
