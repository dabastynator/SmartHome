package de.remote.gpiopower;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.controlcenter.api.IControlUnit;
import de.remote.gpiopower.api.IInternetSwitch;

public class GPIOControlUnit implements IControlUnit{

	private String switchName;
	private String description;
	private IInternetSwitch power;

	public GPIOControlUnit(String switchName, String description, IInternetSwitch power) {
		this.switchName = switchName;
		this.description = description;
		this.power = power;
	}
	
	@Override
	public String getName() throws RemoteException {
		return switchName;
	}

	@Override
	public Class getRemoteableControlInterface() throws RemoteException {
		return IInternetSwitch.class;
	}

	@Override
	public IInternetSwitch getRemoteableControlObject() throws RemoteException {
		return power;
	}

	@Override
	public String getDescription() throws RemoteException {
		return description;
	}

}
