package de.neo.smarthome.gpio;

import de.neo.smarthome.AbstractControlUnit;
import de.neo.smarthome.AbstractUnitHandler;
import de.neo.smarthome.ControlUnitFactory;
import de.neo.smarthome.controlcenter.IControlCenter;

public class GPIOUnitFactory implements ControlUnitFactory {

	private GPIOSender mPower;

	public GPIOUnitFactory() {
		mPower = GPIOSender.getInstance();
	}

	@Override
	public AbstractControlUnit createControlUnit(IControlCenter center) {
		return new GPIOControlUnit(mPower, center);
	}

	@Override
	public AbstractUnitHandler createUnitHandler(IControlCenter center) {
		return new WebSwitchImpl(center);
	}

}
