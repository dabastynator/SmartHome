package de.neo.smarthome;

import de.neo.remote.rmi.RemoteAble;
import de.neo.smarthome.controlcenter.ControlCenter;

public abstract class AbstractUnitHandler implements RemoteAble {

	protected ControlCenter mCenter;

	public AbstractUnitHandler(ControlCenter center) {
		mCenter = center;
	}

	public abstract String getWebPath();

}
