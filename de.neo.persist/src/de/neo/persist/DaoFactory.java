package de.neo.persist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DaoFactory {

	protected static DaoFactory mSingelton;

	public static DaoFactory initiate() {
		if (mSingelton == null)
			mSingelton = new DaoFactory();
		return mSingelton;
	}

	public static DaoFactory getInstance() {
		if (mSingelton == null)
			throw new IllegalStateException("factory has not been initiated. call initiate first");
		return mSingelton;
	}

	protected Map<Class<?>, Dao<?>> mMapClassDao;
	protected List<Dao<?>> mDaoList;

	protected DaoFactory() {
		mMapClassDao = new HashMap<>();
		mDaoList = new ArrayList<>();
	}

	public void registerDao(Class<?> c, Dao<?> dao) {
		mMapClassDao.put(c, dao);
		mDaoList.add(dao);
	}

	@SuppressWarnings("unchecked")
	public <T> Dao<T> getDao(Class<?> domain) {
		return (Dao<T>) mMapClassDao.get(domain);
	}

	public Object getCustomDao(Class<?> domain) {
		return mMapClassDao.get(domain);
	}

}
