package de.neo.remote.mobile.persistence;

import java.util.Map;

import android.content.Context;
import de.neo.android.persistence.Dao;
import de.neo.android.persistence.DaoBuilder;
import de.neo.android.persistence.DatabaseDao;
import de.neo.android.persistence.NeoDataBase;

public class RemoteDaoBuilder extends DaoBuilder {

	public RemoteDaoBuilder(Context context) {
		setDatabase(new NeoDataBase(context, "de.neo.remote", 4));
		setDaoMapFilling(new RemoteDaoFilling());
	}

	class RemoteDaoFilling implements DaoMapFilling {

		@Override
		public void createDaos(Map<Class<?>, Dao<?>> daoMap, DaoBuilder builder) {
			daoMap.put(RemoteServer.class, new DatabaseDao<RemoteServer>(RemoteServer.class, builder));
			daoMap.put(MediaServerState.class, new DatabaseDao<MediaServerState>(MediaServerState.class, builder));
		}
	}

}
