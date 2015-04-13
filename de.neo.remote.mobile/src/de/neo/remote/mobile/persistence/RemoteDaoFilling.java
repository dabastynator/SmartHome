package de.neo.remote.mobile.persistence;

import java.util.Map;

import de.neo.android.persistence.Dao;
import de.neo.android.persistence.DaoBuilder;
import de.neo.android.persistence.DaoBuilder.DaoMapFilling;
import de.neo.android.persistence.DatabaseDao;

public class RemoteDaoFilling implements DaoMapFilling {

	@Override
	public void createDaos(Map<Class<?>, Dao<?>> daoMap, DaoBuilder builder) {
		daoMap.put(RemoteServer.class, new DatabaseDao<RemoteServer>(
				RemoteServer.class, builder));
	}
}
