package de.neo.smarthome;

import de.neo.persist.annotations.Persist;
import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.api.BeanWeb;
import de.neo.smarthome.api.IControllUnit;
import de.neo.smarthome.controlcenter.ControlCenter;

public abstract class AbstractControlUnit implements IControllUnit {

	@Persist(name = "name")
	protected String mName;

	@Persist(name = "id")
	protected String mID;

	protected ControlCenter mCenter;

	public void setName(String name) {
		mName = name;
	}

	public void setId(String id) {
		mID = id;
	}

	@Override
	public void setControlCenter(ControlCenter center) {
		mCenter = center;
	}

	@Override
	public String getName() throws RemoteException {
		return mName;
	}

	@Override
	public String getID() throws RemoteException {
		return mID;
	}

	@Override
	public BeanWeb getWebBean() {
		BeanWeb bean = new BeanWeb();
		bean.mID = mID;
		bean.mID = mName;
		return bean;
	}
}
