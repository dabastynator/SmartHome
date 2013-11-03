package de.neo.remote.mediaserver.impl;

import de.neo.remote.controlcenter.api.IControlUnit;
import de.neo.remote.mediaserver.api.IMediaServer;
import de.neo.rmi.protokol.RemoteException;

public class MediaControlUnit implements IControlUnit {

	private String name;
	private IMediaServer mediaServer;
	private float[] position;

	public MediaControlUnit(String name, IMediaServer mediaServer, float[] position) {
		this.name = name;
		this.mediaServer = mediaServer;
		this.position = position;
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
	public IMediaServer getRemoteableControlObject() throws RemoteException {
		return mediaServer;
	}

	@Override
	public String getDescription() throws RemoteException {
		return "Media server";
	}

	@Override
	public float[] getPosition() throws RemoteException {
		return position;
	}

}
