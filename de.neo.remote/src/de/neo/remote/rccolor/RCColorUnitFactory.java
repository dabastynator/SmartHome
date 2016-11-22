package de.neo.remote.rccolor;

import de.neo.remote.AbstractControlUnit;
import de.neo.remote.AbstractUnitHandler;
import de.neo.remote.ControlUnitFactory;
import de.neo.remote.api.IControlCenter;

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
