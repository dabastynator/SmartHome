package de.neo.smarthome.rccolor;

import de.neo.smarthome.AbstractControlUnit;
import de.neo.smarthome.AbstractUnitHandler;
import de.neo.smarthome.ControlUnitFactory;
import de.neo.smarthome.api.IControlCenter;

public class RCColorUnitFactory implements ControlUnitFactory {

	@Override
	public AbstractControlUnit createControlUnit(IControlCenter center) {
		return new RCColorControlUnit(center);
	}

	@Override
	public AbstractUnitHandler createUnitHandler(IControlCenter center) {
		return new WebLEDStripImpl(center);
	}

}
