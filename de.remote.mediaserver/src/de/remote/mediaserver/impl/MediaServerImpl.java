package de.remote.mediaserver.impl;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.mediaserver.api.IBrowser;
import de.remote.mediaserver.api.IControl;
import de.remote.mediaserver.api.IMediaServer;
import de.remote.mediaserver.api.IPlayList;
import de.remote.mediaserver.api.IPlayer;

public class MediaServerImpl implements IMediaServer {

	private TotemPlayer totem;
	private MPlayer mplayer;
	private ControlImpl control;
	private PlayListImpl playlist;
	private String browserLocation;
	private String name;

	public MediaServerImpl(String browseLocation, String playlistLocation, String name) {
		totem = new TotemPlayer();
		mplayer = new MPlayer(playlistLocation);
		browserLocation = browseLocation;
		control = new ControlImpl();
		playlist = new PlayListImpl(playlistLocation);
		this.name = name;
	}

	@Override
	public IBrowser createBrowser() throws RemoteException {
		return new BrowserImpl(browserLocation);
	}

	@Override
	public IPlayer getTotemPlayer() throws RemoteException {
		return totem;
	}

	@Override
	public IPlayer getMPlayer() throws RemoteException {
		return mplayer;
	}

	@Override
	public IControl getControl() throws RemoteException {
		return control;
	}

	@Override
	public IPlayList getPlayList() throws RemoteException {
		return playlist;
	}

	@Override
	public String getName() throws RemoteException {
		return name;
	}
}