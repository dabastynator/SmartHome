package de.neo.remote.gpio;

import java.io.IOException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.neo.remote.AbstractControlUnit;
import de.neo.remote.api.IInternetSwitch;
import de.neo.rmi.protokol.RemoteException;

public class GPIOControlUnit extends AbstractControlUnit {

	private InternetSwitchImpl mSwitch;
	private SwitchPower mPower;

	public GPIOControlUnit(SwitchPower power) {
		mPower = power;
	}

	@Override
	public Class<?> getRemoteableControlInterface() throws RemoteException {
		return IInternetSwitch.class;
	}

	@Override
	public IInternetSwitch getRemoteableControlObject() throws RemoteException {
		return mSwitch;
	}

	@Override
	public void initialize(Element element) throws SAXException, IOException {
		super.initialize(element);
		mSwitch = new InternetSwitchImpl(mPower);
		mSwitch.initialize(element);
	}

}
