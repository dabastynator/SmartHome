package de.remote.impl;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IBrowser;
import de.remote.api.IChatServer;
import de.remote.api.IControl;
import de.remote.api.IPlayList;
import de.remote.api.IPlayer;
import de.remote.api.IStation;

public class StationImpl implements IStation {

	private TotemPlayer totem;
	private MPlayer mplayer;
	private ControlImpl control;
	private PlayListImpl playlist;
	private ChatServerImpl chatServer;
	private String browserLocation;

	public StationImpl(String location) {
		totem = new TotemPlayer();
		mplayer = new MPlayer();
		browserLocation = location;
		control = new ControlImpl();
		playlist = new PlayListImpl();
		chatServer = new ChatServerImpl();
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
	public IChatServer getChatServer() throws RemoteException {
		return chatServer;
	}

}
