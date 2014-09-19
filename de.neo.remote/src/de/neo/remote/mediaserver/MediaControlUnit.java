package de.neo.remote.mediaserver;

import de.neo.remote.api.IControlUnit;
import de.neo.remote.api.IMediaServer;
import de.neo.rmi.protokol.RemoteException;

public class MediaControlUnit implements IControlUnit {

	private String mName;
	private IMediaServer mMediaServer;
	private float[] mPosition;
	private String mType;

	public MediaControlUnit(String name, IMediaServer mediaServer,
			float[] position, String type) {
		mName = name;
		mMediaServer = mediaServer;
		mPosition = position;
		mType = type;
	}

	@Override
	public String getName() throws RemoteException {
		return mName;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getRemoteableControlInterface() throws RemoteException {
		return IMediaServer.class;
	}

	@Override
	public IMediaServer getRemoteableControlObject() {
		return mMediaServer;
	}

	@Override
	public String getDescription() throws RemoteException {
		return mType;
	}

	@Override
	public float[] getPosition() throws RemoteException {
		return mPosition;
	}

}
