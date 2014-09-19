package de.neo.remote.gpio;

import de.neo.remote.api.IControlUnit;
import de.neo.remote.api.IInternetSwitch;
import de.neo.rmi.protokol.RemoteException;

public class GPIOControlUnit implements IControlUnit{

	private String mSwitchName;
	private String mDescription;
	private IInternetSwitch mPower;
	private float[] mPosition;

	public GPIOControlUnit(String switchName, String description, IInternetSwitch power, float[] position) {
		mSwitchName = switchName;
		mDescription = description;
		mPower = power;
		mPosition = position;
	}
	
	@Override
	public String getName() throws RemoteException {
		return mSwitchName;
	}

	@Override
	public Class<?> getRemoteableControlInterface() throws RemoteException {
		return IInternetSwitch.class;
	}

	@Override
	public IInternetSwitch getRemoteableControlObject() throws RemoteException {
		return mPower;
	}

	@Override
	public String getDescription() throws RemoteException {
		return mDescription;
	}

	@Override
	public float[] getPosition() throws RemoteException {
		return mPosition;
	}

}
