package de.neo.remote.mediaserver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.neo.remote.api.IBrowser;
import de.neo.remote.api.IControl;
import de.neo.remote.api.IDVDPlayer;
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

	public void initialize(Element element) throws SAXException, IOException {
		for (String attribute : new String[] { "location", "playlistLocation" })
			if (!element.hasAttribute(attribute))
				throw new SAXException(attribute + " missing for mediaserver");
		String browseLocation = element.getAttribute("location");
		String playlistLocation = element.getAttribute("playlistLocation");
		boolean thumbnailWorker = true;
		if (element.hasAttribute("thumbnailWorker")) {
			String worker = element.getAttribute("thumbnailWorker");
			thumbnailWorker = worker.equals("true") || worker.equals("1");
		}

		if (!new File(browseLocation).exists())
			throw new IOException("Browser location does not exist: " + browseLocation);
		if (!new File(playlistLocation).exists())
			throw new IOException("Playlist location does not exist: " + playlistLocation);
		ThumbnailHandler.init(playlistLocation, thumbnailWorker);
		mTotem = new TotemPlayer();
		mMplayer = new MPlayerDVD(playlistLocation);
		mBrowserLocation = browseLocation;
		if (!mBrowserLocation.endsWith(File.separator))
			mBrowserLocation += File.separator;
		mControl = new ControlImpl();
		mPlaylist = new PlayListImpl(playlistLocation);
		mOmxplayer = new OMXPlayer();
	}

	@Override
	public String[] listDirectories(String path) throws RemoteException {
		if (path.startsWith("..") || path.contains(File.separator + ".."))
			throw new IllegalArgumentException("Path must not contain '..'");
		String location = mBrowserLocation + path;
		if (!location.endsWith(File.separator))
			location += File.separator;
		List<String> list = new ArrayList<String>();
		for (String str : new File(location).list())
			if (new File(location + str).isDirectory())
				if (str.length() > 0 && str.charAt(0) != '.')
					list.add(str);
		return list.toArray(new String[] {});
	}

	@Override
	public String[] listFiles(String path) throws RemoteException {
		if (path.startsWith("..") || path.contains(File.separator + ".."))
			throw new IllegalArgumentException("Path must not contain '..'");
		String location = mBrowserLocation + path;
		if (!location.endsWith(File.separator))
			location += File.separator;
		List<String> list = new ArrayList<String>();
		for (String str : new File(location).list())
			if (new File(location + str).isFile())
				if (str.length() > 0 && str.charAt(0) != '.')
					list.add(str);
		return list.toArray(new String[] {});
	}

	@Override
	public String getBrowserPath() {
		return mBrowserLocation;
	}

}
