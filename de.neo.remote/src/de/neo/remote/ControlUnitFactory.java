package de.neo.remote;

import de.neo.remote.api.IControlCenter;

public interface ControlUnitFactory {

	public AbstractControlUnit createControlUnit(IControlCenter center);
}
