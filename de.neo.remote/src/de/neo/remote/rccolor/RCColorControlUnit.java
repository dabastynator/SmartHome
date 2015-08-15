package de.neo.remote.rccolor;

import de.neo.remote.AbstractControlUnit;
import de.neo.remote.api.Event;
import de.neo.remote.api.IControlCenter;
import de.neo.remote.api.IRCColor;
import de.neo.rmi.protokol.RemoteException;

public class RCColorControlUnit extends AbstractControlUnit{

	public RCColorControlUnit(IControlCenter center) {
		super(center);
	}

	@Override
	public Class getRemoteableControlInterface() throws RemoteException {
		return IRCColor.class;
	}

	@Override
	public Object getRemoteableControlObject() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean performEvent(Event event) throws RemoteException,
			EventException {
		String colorString = event.getParameter("color");
		if (colorString == null)
			throw new EventException(
					"Parameter color missing to set the color!");
		int color = 0;
		try{
			color = Integer.parseInt(colorString);
		}catch(Exception e){
			throw new EventException(
					"Cannot parse color code");
		}
		return false;
	}

}
