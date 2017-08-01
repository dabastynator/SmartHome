package de.neo.smarthome;

import de.neo.remote.rmi.RemoteAble;
import de.neo.smarthome.controlcenter.IControlCenter;

public abstract class AbstractUnitHandler implements RemoteAble {

	protected IControlCenter mCenter;

	public AbstractUnitHandler(IControlCenter center) {
		mCenter = center;
	}

	public abstract String getWebPath();

}
