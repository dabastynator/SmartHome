package de.neo.remote;

import de.neo.remote.api.IControlCenter;
import de.neo.rmi.protokol.RemoteAble;

public interface ControlUnitFactory extends RemoteAble {

	public AbstractControlUnit createControlUnit(IControlCenter center);

	public AbstractUnitHandler createUnitHandler(IControlCenter center);
}
