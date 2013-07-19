package de.remote.controlcenter.impl;

import java.util.ArrayList;
import java.util.List;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.controlcenter.api.IControlCenter;
import de.remote.controlcenter.api.IControlUnit;

/**
 * Implement the control center interface.
 * 
 * @author sebastian
 */
public class ControlCenterImpl implements IControlCenter {

	/**
	 * List of all control units
	 */
	private List<IControlUnit> controlUnits = new ArrayList<IControlUnit>();

	/**
	 * Path to the configuration file.
	 */
	private String configurationFile;

	/**
	 * allocate new control center. it checks every 5 minutes all control units
	 * and removes units with exception.
	 * 
	 * @param config
	 */
	public ControlCenterImpl(String config) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000 * 60 * 5);
				} catch (InterruptedException e) {
				}
				checkControlUnits();
			}
		};
		thread.start();
		configurationFile = config;
	}

	/**
	 * remove control units wit exception
	 */
	private void checkControlUnits() {
		List<IControlUnit> exceptionList = new ArrayList<IControlUnit>();
		for (IControlUnit unit : controlUnits) {
			try {
				unit.getName();
			} catch (RemoteException e) {
				exceptionList.add(unit);
			}
		}
		controlUnits.removeAll(exceptionList);
	}

	@Override
	public int getControlUnitNumber() throws RemoteException {
		return controlUnits.size();
	}

	@Override
	public void addControlUnit(IControlUnit controlUnit) throws RemoteException {
		controlUnits.add(controlUnit);
		System.out.println("Add control unit: " + controlUnit.getName());
	}

	@Override
	public void removeControlUnit(IControlUnit controlUnit)
			throws RemoteException {
		controlUnits.remove(controlUnit);
	}

	@Override
	public IControlUnit getControlUnit(int number) throws RemoteException {
		return controlUnits.get(number);
	}

}
