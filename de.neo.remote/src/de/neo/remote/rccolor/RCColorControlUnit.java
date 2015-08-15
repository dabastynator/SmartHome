package de.neo.remote.rccolor;

import de.neo.remote.AbstractControlUnit;
import de.neo.remote.api.Event;
import de.neo.remote.api.IControlCenter;
import de.neo.remote.api.IRCColor;
import de.neo.rmi.protokol.RemoteException;

public class RCColorControlUnit extends AbstractControlUnit {

	private RCColor mColorUnit;

	public RCColorControlUnit(IControlCenter center) {
		super(center);
		mColorUnit = new RCColor();
	}

	@Override
	public Class getRemoteableControlInterface() throws RemoteException {
		return IRCColor.class;
	}

	@Override
	public IRCColor getRemoteableControlObject() throws RemoteException {
		return mColorUnit;
	}

	@Override
	public boolean performEvent(Event event) throws RemoteException,
			EventException {
		String colorString = event.getParameter("color");
		if (colorString == null)
			throw new EventException(
					"Parameter color missing to set the color!");
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
