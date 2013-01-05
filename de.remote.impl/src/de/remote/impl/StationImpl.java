package de.remote.impl;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IBrowser;
import de.remote.api.IControl;
import de.remote.api.IMusicStation;
import de.remote.api.IPlayList;
import de.remote.api.IPlayer;

public class StationImpl implements IMusicStation {

	private TotemPlayer totem;
	private MPlayer mplayer;
	private ControlImpl control;
	private PlayListImpl playlist;
	private String browserLocation;

	public StationImpl(String browseLocation, String playlistLocation) {
		totem = new TotemPlayer();
		mplayer = new MPlayer(playlistLocation);
		browserLocation = browseLocation;
		control = new ControlImpl();
		playlist = new PlayListImpl(playlistLocation);
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
}
