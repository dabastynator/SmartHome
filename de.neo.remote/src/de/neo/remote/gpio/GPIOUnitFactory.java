package de.neo.remote.gpio;

import de.neo.remote.AbstractControlUnit;
import de.neo.remote.AbstractUnitHandler;
import de.neo.remote.ControlUnitFactory;
import de.neo.remote.api.IControlCenter;

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
