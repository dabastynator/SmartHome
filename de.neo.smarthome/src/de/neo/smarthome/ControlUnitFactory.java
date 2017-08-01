package de.neo.smarthome;

import de.neo.remote.rmi.RemoteAble;
import de.neo.smarthome.controlcenter.IControlCenter;

public interface ControlUnitFactory extends RemoteAble {

	public AbstractControlUnit createControlUnit(IControlCenter center);

	public AbstractUnitHandler createUnitHandler(IControlCenter center);
}
