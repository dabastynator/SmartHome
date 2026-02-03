package de.neo.smarthome.mediaserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import de.neo.persist.annotations.Domain;
import de.neo.persist.annotations.OnLoad;
import de.neo.persist.annotations.Persist;
import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.AbstractControlUnit;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.api.IWebMediaServer.BeanFileSystem;
import de.neo.smarthome.api.IWebMediaServer.FileType;

@Domain(name = "MediaServer")
public class MediaControlUnit extends AbstractControlUnit {

	public static final int DOWNLOAD_PORT = 5033;
	
	public static final String Cover = ".cover_art.jpg";
	public static final String Collection = ".collection.jpg";

	@Persist(name = "location")
	private String mBrowserLocation;

	@Persist(name = "playlistLocation")
	private String mPlaylistLocation;

	static private TotemPlayer mTotem;
	static private MPlayer mMplayer;
	static private OMXPlayer mOmxplayer;

	private PlayList mPlaylist;

	@OnLoad
	public void onLoad() {
		if (!new File(mBrowserLocation).exists())
			RemoteLogger.performLog(LogPriority.ERROR, "Browser location does not exist: " + mBrowserLocation,
					"MediaServer");
		if (!new File(mPlaylistLocation).exists())
			RemoteLogger.performLog(LogPriority.ERROR, "Playlist location does not exist: " + mPlaylistLocation,
					"MediaServer");

		if (!mBrowserLocation.endsWith(File.separator))
			mBrowserLocation += File.separator;
		mPlaylist = new PlayList(mPlaylistLocation);

		if (mTotem == null) {
			mTotem = new TotemPlayer();
			mMplayer = new MPlayer(mPlaylistLocation);
			mOmxplayer = new OMXPlayer();
		}
	}

	public void setBrowserLocation(String locationBrowser) {
		mBrowserLocation = locationBrowser;
	}

	public void setPlaylistLocation(String locationPlaylist) {
		mPlaylistLocation = locationPlaylist;
	}

	public IPlayer getTotemPlayer() throws RemoteException {
		return mTotem;
	}

	public IPlayer getMPlayer() throws RemoteException {
		return mMplayer;
	}

	public PlayList getPlayList() throws RemoteException {
		return mPlaylist;
	}

	public IPlayer getOMXPlayer() throws RemoteException {
		return mOmxplayer;
	}

	public String[] listDirectories(String path) throws RemoteException {
		if (path.startsWith("..") || path.contains(File.separator + ".."))
			throw new IllegalArgumentException("Path must not contain '..'");
		String location = mBrowserLocation + path;
		if (!location.endsWith(File.separator))
			location += File.separator;
		List<String> list = new ArrayList<String>();
		File directory = new File(location);
		if (!directory.isDirectory())
		{
			throw new RemoteException("Invalid path");
		}
		for (String str : directory.list())
			if (new File(location + str).isDirectory())
				if (str.length() > 0 && str.charAt(0) != '.')
					list.add(str);
		return list.toArray(new String[] {});
	}

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

	public String getBrowserPath() {
		return mBrowserLocation;
	}

	public ArrayList<BeanFileSystem> search(String path, String target) {
		path = mBrowserLocation + path;
		ArrayList<BeanFileSystem> matches = new ArrayList<>();
		try
		{
			Process exec = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", " find '" + path + "' -type d -path '*/.*' -prune -o -not -name '.*' -type f -iname '*" + target + "*' -print | sort" });
			BufferedReader input = new BufferedReader(new InputStreamReader(exec.getInputStream()));
			BufferedReader error = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
			String line = "";
			while ((line = input.readLine()) != null) 
			{
				BeanFileSystem bean = new BeanFileSystem();
				File file = new File(line);
				bean.path = file.getAbsolutePath().substring(mBrowserLocation.length());
				bean.name = file.getName();
				if (file.isDirectory())
				{
					bean.fileType = FileType.Directory;
				}
				else if (file.isFile())
				{
					bean.fileType = FileType.File;
				}
				matches.add(bean);
			}
			input.close();
			error.close();
		} 
		catch (IOException e) 
		{
			RemoteLogger.performLog(LogPriority.ERROR, e.getClass().getSimpleName() + ": " + e.getMessage(), "MPlayer");
		}
		return matches;
	}

	public boolean isFile(String path) {
		return new File (mBrowserLocation + path).isFile();
	}

}
