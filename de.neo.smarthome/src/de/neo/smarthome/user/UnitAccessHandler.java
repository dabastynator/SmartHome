package de.neo.smarthome.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.neo.persist.Dao;
import de.neo.persist.DaoException;
import de.neo.persist.DaoFactory;
import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.api.IControlCenter;
import de.neo.smarthome.api.IControllUnit;

public class UnitAccessHandler {

	private Map<User, UserAccessList> mAccess = new HashMap<>();

	private IControlCenter mCenter;

	public UnitAccessHandler(IControlCenter center) {
		mCenter = center;
	}

	public void initialize() {
		try {
			Dao<UnitAccess> accessDao = DaoFactory.getInstance().getDao(UnitAccess.class);
			Dao<User> userDao = DaoFactory.getInstance().getDao(User.class);
			for (UnitAccess access : accessDao.loadAll()) {
				if (access.getUser() == null) {
					access.setUser(userDao.loadById(access.getUserId()));
				}
				if (access.getUnit() == null) {
					access.setUnit(mCenter.getControlUnit(access.getUnitId()));
				}
				UserAccessList accessList = getAccessListByUser(access.getUser());
				mCenter.getControlUnits().get(access.getUnitId());
				accessList.mUnits.put(access.getUnitId(), access);
			}
		} catch (DaoException e) {
			RemoteLogger.performLog(LogPriority.ERROR, e.getClass().getSimpleName() + ": " + e.getMessage(),
					this.getClass().getSimpleName());
		} catch (RemoteException e) {
			RemoteLogger.performLog(LogPriority.ERROR, e.getClass().getSimpleName() + ": " + e.getMessage(),
					this.getClass().getSimpleName());
		}
	}

	public UserAccessList getAccessListByUser(User user) {
		UserAccessList access = mAccess.get(user);
		if (access == null) {
			access = new UserAccessList(user);
			mAccess.put(user, access);
		}
		return access;
	}

	public <T> T require(String token, String id) throws RemoteException {
		User user = UserSessionHandler.require(token);
		return require(user, id);
	}

	@SuppressWarnings("unchecked")
	public <T> T require(User user, String id) throws RemoteException {
		UserAccessList list = mAccess.get(user);
		if (list != null) {
			IControllUnit unit = list.getUnit(id);
			try {
				if (unit != null) {
					return (T) unit;
				}
			} catch (ClassCastException e) {
				throw new RemoteException("Unit of false type");
			}
		}
		throw new RemoteException("Access for " + id + " denied");
	}

	public ArrayList<IControllUnit> unitsFor(User user) {
		ArrayList<IControllUnit> units = new ArrayList<>();
		UserAccessList list = getAccessListByUser(user);
		if (list != null) {
			for (UnitAccess access : list.listAccess()) {
				units.add(access.getUnit());
			}
		}
		return units;
	}

	public static class UserAccessList {

		protected User mUser;

		private Map<String, UnitAccess> mUnits = new HashMap<>();

		public UserAccessList(User user) {
			mUser = user;
		}

		public IControllUnit getUnit(String unitId) {
			UnitAccess access = mUnits.get(unitId);
			if (access != null) {
				return access.getUnit();
			}
			return null;
		}

		public void addAccess(UnitAccess access) {
			mUnits.put(access.getUnitId(), access);
		}

		public void removeAccess(UnitAccess access) {
			mUnits.remove(access.getUnitId());
		}

		public Collection<UnitAccess> listAccess() {
			return mUnits.values();
		}
	}

}
