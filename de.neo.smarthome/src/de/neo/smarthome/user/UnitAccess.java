package de.neo.smarthome.user;

import de.neo.persist.annotations.Domain;
import de.neo.persist.annotations.Persist;
import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.api.IControllUnit;

@Domain(name = "UnitAccess")
public class UnitAccess {

	@Persist(name = "user")
	private long mUserId;

	@Persist(name = "unit")
	private String mUnitId;

	private User mUser;

	private IControllUnit mUnit;

	public long getUserId() {
		return mUserId;
	}

	public void setUserId(Long userId) {
		mUserId = userId;
	}

	public String getUnitId() {
		return mUnitId;
	}

	public void setUnitId(String unitId) {
		mUnitId = unitId;
	}

	public User getUser() {
		return mUser;
	}

	public void setUser(User user) {
		mUser = user;
		mUserId = user.getId();
	}

	public IControllUnit getUnit() {
		return mUnit;
	}

	public void setUnit(IControllUnit unit) throws RemoteException {
		mUnit = unit;
		mUnitId = unit.getID();
	}

}
