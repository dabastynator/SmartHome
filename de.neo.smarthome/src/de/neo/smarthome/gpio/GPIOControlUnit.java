package de.neo.smarthome.gpio;

import java.io.IOException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.neo.remote.protokol.RemoteException;
import de.neo.smarthome.AbstractControlUnit;
import de.neo.smarthome.api.Event;
import de.neo.smarthome.api.IControlCenter;
import de.neo.smarthome.api.IControlUnit.EventException;
import de.neo.smarthome.api.IInternetSwitch;
import de.neo.smarthome.api.IWebSwitch.State;

public class GPIOControlUnit extends AbstractControlUnit {

	private InternetSwitchImpl mSwitch;
	private GPIOSender mPower;

	public GPIOControlUnit(GPIOSender power, IControlCenter center) {
		super(center);
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
		mSwitch = new InternetSwitchImpl(mPower, this);
		mSwitch.initialize(element);
	}

	@Override
	public boolean performEvent(Event event) throws RemoteException, EventException {
		String state = event.getParameter("state");
		if (state == null)
			throw new EventException("Parameter state (on|off) missing to execute switch event!");
		if (state.equalsIgnoreCase("on"))
			mSwitch.setState(State.ON);
		else if (state.equalsIgnoreCase("off"))
			mSwitch.setState(State.OFF);
		else
			throw new EventException("Unknown parameter-value for switch-event '" + state + "'! Excpected: on|off");
		return true;
	}

}
