package de.neo.remote.mediaserver;

import de.neo.remote.api.IBrowser;
import de.neo.remote.api.IControl;
import de.neo.remote.api.IDVDPlayer;
import de.neo.remote.api.IImageViewer;
import de.neo.remote.api.IMediaServer;
import de.neo.remote.api.IPlayList;
import de.neo.remote.api.IPlayer;
import de.neo.rmi.protokol.RemoteException;

public class MediaServerImpl implements IMediaServer {

	public static final String ROOT = "MediaServer";

	private TotemPlayer mTotem;
	private MPlayerDVD mMplayer;
	private ControlImpl mControl;
	private PlayListImpl mPlaylist;
	private String mBrowserLocation;
	private OMXPlayer mOmxplayer;
	private ImageViewerImpl mImageViewer;

	public MediaServerImpl(String browseLocation, String playlistLocation,
			boolean thumbnailWorker) {
		ThumbnailHandler.init(playlistLocation, thumbnailWorker);
		mTotem = new TotemPlayer();
		mMplayer = new MPlayerDVD(playlistLocation);
		mBrowserLocation = browseLocation;
		mControl = new ControlImpl();
		mPlaylist = new PlayListImpl(playlistLocation);
		mOmxplayer = new OMXPlayer();
		mImageViewer = new ImageViewerImpl();
	}

	@Override
	public IBrowser createBrowser() throws RemoteException {
		return new BrowserImpl(mBrowserLocation);
	}

	@Override
	public IPlayer getTotemPlayer() throws RemoteException {
		return mTotem;
	}

	@Override
	public IDVDPlayer getMPlayer() throws RemoteException {
		return mMplayer;
	}

	@Override
	public IControl getControl() throws RemoteException {
		return mControl;
	}

	@Override
	public IPlayList getPlayList() throws RemoteException {
		return mPlaylist;
	}

	@Override
	public IPlayer getOMXPlayer() throws RemoteException {
		return mOmxplayer;
	}

	@Override
	public IImageViewer getImageViewer() throws RemoteException {
		return mImageViewer;
	}

}
