package de.neo.remote.action;

import de.neo.remote.AbstractControlUnit;
import de.neo.remote.ControlUnitFactory;

public class ActionUnitFactory implements ControlUnitFactory {

	@Override
	public AbstractControlUnit createControlUnit() {
		return new ActionControlUnit();
	}

}
