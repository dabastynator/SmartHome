package de.neo.smarthome.rccolor;

import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.AbstractControlUnit;
import de.neo.smarthome.api.Event;
import de.neo.smarthome.controlcenter.IControlCenter;

public class RCColorControlUnit extends AbstractControlUnit {

	private RCColor mColorUnit;

	public RCColorControlUnit(IControlCenter center) {
		super(center);
		mColorUnit = new RCColor();
	}

	@Override
	public RCColor getControllObject() throws RemoteException {
		return mColorUnit;
	}

	@Override
	public boolean performEvent(Event event) throws RemoteException, EventException {
		String colorString = event.getParameter("color");
		if (colorString == null)
			throw new EventException("Parameter color missing to set the color!");
		int color = 0, duration = 0;
		try {
			color = Integer.parseInt(colorString);
			String durationString = event.getParameter("duration");
			if (durationString != null)
				duration = Integer.parseInt(durationString);
		} catch (Exception e) {
			throw new EventException("Cannot parse color code or duration");
		}
		if (duration == 0)
			mColorUnit.setColor(color);
		else
			mColorUnit.setColor(color, duration);
		return true;
	}
}
