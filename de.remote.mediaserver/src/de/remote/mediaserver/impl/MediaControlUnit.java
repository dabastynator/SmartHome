package de.remote.mediaserver.impl;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.controlcenter.api.IControlUnit;
import de.remote.mediaserver.api.IMediaServer;

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
