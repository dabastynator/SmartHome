package de.neo.remote.mediaserver;

import de.neo.remote.api.IControlUnit;
import de.neo.remote.api.IMediaServer;
import de.neo.rmi.protokol.RemoteException;

public class MediaControlUnit implements IControlUnit {

	private String name;
	private IMediaServer mediaServer;
	private float[] position;
	private String type;

	public MediaControlUnit(String name, IMediaServer mediaServer,
			float[] position, String type) {
		this.name = name;
		this.mediaServer = mediaServer;
		this.position = position;
		this.type = type;
	}

	@Override
	public String getName() throws RemoteException {
		return name;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getRemoteableControlInterface() throws RemoteException {
		return IMediaServer.class;
	}

	@Override
	public IMediaServer getRemoteableControlObject() {
		return mediaServer;
	}

	@Override
	public String getDescription() throws RemoteException {
		return type;
	}

	@Override
	public float[] getPosition() throws RemoteException {
		return position;
	}

}
