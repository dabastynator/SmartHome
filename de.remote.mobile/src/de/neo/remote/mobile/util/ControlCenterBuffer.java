package de.remote.mobile.util;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.controlcenter.api.GroundPlot;
import de.remote.controlcenter.api.IControlCenter;
import de.remote.controlcenter.api.IControlUnit;

public class ControlCenterBuffer implements IControlCenter {

	private IControlCenter center;
	private int number;
	private GroundPlot ground;
	private IControlUnit[] units;

	public ControlCenterBuffer(IControlCenter center) {
		this.center = center;
		clear();
	}

	public void clear() {
		number = -1;
		ground = null;
	}

	@Override
	public int getControlUnitNumber() throws RemoteException {
		if (number < 0) {
			number = center.getControlUnitNumber();
			units = new IControlUnit[number];
		}
		return number;
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
	public IControlUnit getControlUnit(int number) throws RemoteException {
		if (units[number] == null)
			units[number] = center.getControlUnit(number);
		return units[number];
	}
}
