package de.neo.remote.gpiopower;

import de.neo.remote.controlcenter.api.IControlUnit;
import de.neo.remote.gpiopower.api.IInternetSwitch;
import de.neo.rmi.protokol.RemoteException;

public class GPIOControlUnit implements IControlUnit{

	private String switchName;
	private String description;
	private IInternetSwitch power;
	private float[] position;

	public GPIOControlUnit(String switchName, String description, IInternetSwitch power, float[] position) {
		this.switchName = switchName;
		this.description = description;
		this.power = power;
		this.position = position;
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

	@Override
	public float[] getPosition() throws RemoteException {
		return position;
	}

}
