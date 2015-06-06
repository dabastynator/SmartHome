package de.neo.remote.gpio;

import de.neo.remote.AbstractControlUnit;
import de.neo.remote.ControlUnitFactory;
import de.neo.remote.api.IControlCenter;

public class GPIOUnitFactory implements ControlUnitFactory {

	private SwitchPower mPower;

	public GPIOUnitFactory() {
		mPower = new SwitchPower();
	}

	@Override
	public AbstractControlUnit createControlUnit(IControlCenter center) {
		return new GPIOControlUnit(mPower, center);
	}

}
