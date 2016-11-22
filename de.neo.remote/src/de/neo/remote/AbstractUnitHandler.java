package de.neo.remote;

import de.neo.remote.api.IControlCenter;
import de.neo.rmi.protokol.RemoteAble;

public abstract class AbstractUnitHandler implements RemoteAble {

	protected IControlCenter mCenter;

	public AbstractUnitHandler(IControlCenter center) {
		mCenter = center;
	}

	public abstract String getWebPath();

}
