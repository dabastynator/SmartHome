package de.neo.smarthome.action;

import de.neo.smarthome.AbstractControlUnit;
import de.neo.smarthome.AbstractUnitHandler;
import de.neo.smarthome.ControlUnitFactory;
import de.neo.smarthome.controlcenter.IControlCenter;

public class ActionUnitFactory implements ControlUnitFactory {

	@Override
	public AbstractControlUnit createControlUnit(IControlCenter center) {
		return new ActionControlUnit(center);
	}

	@Override
	public AbstractUnitHandler createUnitHandler(IControlCenter center) {
		return new WebActionImpl(center);
	}

}
