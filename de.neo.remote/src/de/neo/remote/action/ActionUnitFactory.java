package de.neo.remote.action;

import de.neo.remote.AbstractControlUnit;
import de.neo.remote.ControlUnitFactory;
import de.neo.remote.api.IControlCenter;

public class ActionUnitFactory implements ControlUnitFactory {

	@Override
	public AbstractControlUnit createControlUnit(IControlCenter center) {
		return new ActionControlUnit(center);
	}

}
