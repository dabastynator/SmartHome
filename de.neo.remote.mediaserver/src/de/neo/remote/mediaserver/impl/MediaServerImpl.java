package de.neo.remote.mediaserver.impl;

import de.neo.remote.mediaserver.api.IBrowser;
import de.neo.remote.mediaserver.api.IControl;
import de.neo.remote.mediaserver.api.IDVDPlayer;
import de.neo.remote.mediaserver.api.IImageViewer;
import de.neo.remote.mediaserver.api.IMediaServer;
import de.neo.remote.mediaserver.api.IPlayList;
import de.neo.remote.mediaserver.api.IPlayer;
import de.neo.rmi.protokol.RemoteException;

public class MediaServerImpl implements IMediaServer {

	private TotemPlayer totem;
	private MPlayerDVD mplayer;
	private ControlImpl control;
	private PlayListImpl playlist;
	private String browserLocation;
	private OMXPlayer omxplayer;
	private ImageViewerImpl imageViewer;

	public MediaServerImpl(String browseLocation, String playlistLocation) {
		totem = new TotemPlayer();
		mplayer = new MPlayerDVD(playlistLocation);
		browserLocation = browseLocation;
		control = new ControlImpl();
		playlist = new PlayListImpl(playlistLocation);
		omxplayer = new OMXPlayer();
		imageViewer = new ImageViewerImpl();
		ThumbnailHandler.init(playlistLocation);
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
	public IDVDPlayer getMPlayer() throws RemoteException {
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
	public IPlayer getOMXPlayer() throws RemoteException {
		return omxplayer;
	}

	@Override
	public IImageViewer getImageViewer() throws RemoteException {
		return imageViewer;
	}

}
