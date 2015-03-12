package de.neo.remote.mobile.util;

import java.util.HashMap;
import java.util.Map;

import de.neo.remote.api.GroundPlot;
import de.neo.remote.api.IControlCenter;
import de.neo.remote.api.IControlUnit;
import de.neo.rmi.protokol.RemoteException;

public class ControlCenterBuffer implements IControlCenter {

	private IControlCenter center;
	private GroundPlot ground;
	private Map<String, IControlUnit> units;
	private String[] ids;

	public ControlCenterBuffer(IControlCenter center) {
		this.center = center;
		units = new HashMap<String, IControlUnit>();
		clear();
	}

	public void clear() {
		ground = null;
		units.clear();
	}

	@Override
	public void addControlUnit(IControlUnit controlUnit) throws RemoteException {
		center.addControlUnit(controlUnit);
		clear();
	}

	@Override
	public GroundPlot getGroundPlot() throws RemoteException {
		if (ground == null)
			ground = center.getGroundPlot();
		return ground;
	}

	@Override
	public void removeControlUnit(IControlUnit controlUnit)
			throws RemoteException {
		center.removeControlUnit(controlUnit);
		clear();
	}

	@Override
	public String[] getControlUnitIDs() throws RemoteException {
		if (ids == null)
			ids = center.getControlUnitIDs();
		return ids;
	}

	@Override
	public IControlUnit getControlUnit(String id) throws RemoteException {
		IControlUnit unit = units.get(id);
		if (unit == null){
			unit = center.getControlUnit(id);
			if (unit != null)
				units.put(id, unit);
		}
		return unit;
	}
}
