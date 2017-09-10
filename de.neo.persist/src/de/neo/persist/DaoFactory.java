package de.neo.persist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DaoFactory {

	protected static DaoFactory mSingelton;

	public static DaoFactory initiate(FactoryBuilder builder) throws DaoException {
		if (mSingelton == null)
			mSingelton = builder.createDaoFactory();
		return mSingelton;
	}

	public static DaoFactory getInstance() {
		if (mSingelton == null)
			throw new IllegalStateException("factory has not been initiated. call initiate first");
		return mSingelton;
	}

	protected Map<Class<?>, Dao<?>> mMapClassDao = new HashMap<>();
	protected List<Dao<?>> mDaoList = new ArrayList<>();

	protected DaoFactory() {
	}

	@SuppressWarnings("unchecked")
	public <T> Dao<T> getDao(Class<?> domain) {
		return (Dao<T>) mMapClassDao.get(domain);
	}

	public Object getCustomDao(Class<?> domain) {
		return mMapClassDao.get(domain);
	}

	public static abstract class FactoryBuilder {

		protected List<Dao<?>> mDaoList = new ArrayList<>();

		public FactoryBuilder registerDao(Dao<?> dao) {
			mDaoList.add(dao);
			return this;
		}

		public abstract DaoFactory createDaoFactory() throws DaoException;

	}

}
