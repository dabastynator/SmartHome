package de.neo.remote.mobile.persistence;

import java.io.IOException;
import java.util.ArrayList;

import de.neo.android.persistence.DomainBase;
import de.neo.android.persistence.Persistent;
import de.neo.remote.api.IWebMediaServer;
import de.neo.remote.api.IWebMediaServer.BeanDownload;
import de.neo.remote.api.IWebMediaServer.BeanFileSystem;
import de.neo.remote.api.IWebMediaServer.BeanMediaServer;
import de.neo.remote.api.IWebMediaServer.BeanPlaylist;
import de.neo.remote.api.IWebMediaServer.BeanPlaylistItem;
import de.neo.remote.api.PlayerException;
import de.neo.remote.api.PlayingBean;
import de.neo.rmi.protokol.RemoteException;

public class MediaServerState extends DomainBase {

	@Persistent
	private String mMediaServerID;

	@Persistent
	private String mPlayer;

	@Persistent
	private String mBrowserLocation;

	private IWebMediaServer mWebMediaServer;

	private RemoteServer mRemoteServer;

	public void initialize(IWebMediaServer webMediaServer, RemoteServer remoteServer) {
		mWebMediaServer = webMediaServer;
		mRemoteServer = remoteServer;
	}

	public RemoteServer getRemoteServer() {
		return mRemoteServer;
	}

	public String getPlayer() {
		return mPlayer;
	}

	public void setPlayer(String player) {
		mPlayer = player;
	}

	public String getBrowserLocation() {
		return mBrowserLocation;
	}

	public void setBrowserLocation(String browserLocation) {
		mBrowserLocation = browserLocation;
	}

	public String getMediaServerID() {
		return mMediaServerID;
	}

	public void setMediaServerID(String mediaServerID) {
		mMediaServerID = mediaServerID;
	}

	public PlayingBean playPause() throws RemoteException, PlayerException {
		return mWebMediaServer.playPause(mMediaServerID, mPlayer);
	}

	public PlayingBean next() throws RemoteException, PlayerException {
		return mWebMediaServer.playNext(mMediaServerID, mPlayer);

	}

	public PlayingBean previous() throws RemoteException, PlayerException {
		return mWebMediaServer.playPrevious(mMediaServerID, mPlayer);
	}

	public PlayingBean volUp() throws RemoteException, PlayerException {
		ArrayList<BeanMediaServer> list = mWebMediaServer.getMediaServer(mMediaServerID);
		if (list.size() > 0 && list.get(0).getCurrentPlaying() != null) {
			int volume = list.get(0).getCurrentPlaying().getVolume();
			return mWebMediaServer.setVolume(mMediaServerID, mPlayer, Math.min(100, volume + 5));
		}
		return null;
	}

	public PlayingBean volDown() throws RemoteException, PlayerException {
		ArrayList<BeanMediaServer> list = mWebMediaServer.getMediaServer(mMediaServerID);
		if (list.size() > 0 && list.get(0).getCurrentPlaying() != null) {
			int volume = list.get(0).getCurrentPlaying().getVolume();
			return mWebMediaServer.setVolume(mMediaServerID, mPlayer, Math.max(0, volume - 5));
		}
		return null;
	}

	public PlayingBean seekBackwards() throws RemoteException, PlayerException {
		return mWebMediaServer.playSeekBackward(mMediaServerID, mPlayer);
	}

	public PlayingBean seekForwards() throws RemoteException, PlayerException {
		return mWebMediaServer.playSeekForward(mMediaServerID, mPlayer);
	}

	public PlayingBean fullScreen(boolean fullscreen) throws RemoteException, PlayerException {
		return mWebMediaServer.playSetFullscreen(mMediaServerID, mPlayer, fullscreen);
	}

	public PlayingBean stop() throws RemoteException, PlayerException {
		return mWebMediaServer.playStop(mMediaServerID, mPlayer);
	}

	public ArrayList<BeanPlaylist> getPlayLists() throws RemoteException {
		return mWebMediaServer.getPlaylists(mMediaServerID);
	}

	public ArrayList<BeanPlaylistItem> getPlayListContent(String playlist) throws RemoteException, PlayerException {
		return mWebMediaServer.getPlaylistContent(mMediaServerID, playlist);
	}

	public PlayingBean playPlaylist(String playlist) throws RemoteException, PlayerException {
		return mWebMediaServer.playPlaylist(mMediaServerID, mPlayer, playlist);
	}

	public PlayingBean playFile(String file) throws RemoteException, PlayerException {
		return mWebMediaServer.playFile(mMediaServerID, mPlayer, file);
	}

	public PlayingBean getPlaying() throws RemoteException {
		ArrayList<BeanMediaServer> list = mWebMediaServer.getMediaServer(mMediaServerID);
		if (list.size() > 0)
			return list.get(0).getCurrentPlaying();
		return null;
	}

	public PlayingBean setVolume(int volume) throws RemoteException, PlayerException {
		return mWebMediaServer.setVolume(mMediaServerID, mPlayer, volume);
	}

	public void extendPlayList(String playlist, String item) throws RemoteException, PlayerException {
		String file = mBrowserLocation;
		if (file.length() > 0 && !file.endsWith(IWebMediaServer.FileSeparator))
			file += IWebMediaServer.FileSeparator;
		file += item;
		mWebMediaServer.playlistExtend(mMediaServerID, playlist, file);
	}

	public void playlistDelete(String playlist) throws RemoteException, PlayerException {
		mWebMediaServer.playlistDelete(mMediaServerID, playlist);
	}

	public void playlistDeleteItem(String playlist, String item) throws RemoteException, PlayerException {
		mWebMediaServer.playlistDeleteItem(mMediaServerID, playlist, item);
	}

	public void playlistCreate(String playlist) throws RemoteException {
		mWebMediaServer.playlistCreate(mMediaServerID, playlist);
	}

	public void goTo(String goTo) {
		mBrowserLocation += IWebMediaServer.FileSeparator + goTo;
	}

	public boolean goBack() {
		if (mBrowserLocation.contains(IWebMediaServer.FileSeparator)) {
			mBrowserLocation = mBrowserLocation.substring(0,
					mBrowserLocation.lastIndexOf(IWebMediaServer.FileSeparator));
			return true;
		}
		return false;
	}

	public ArrayList<BeanFileSystem> getFiles() throws RemoteException {
		return mWebMediaServer.getFiles(mMediaServerID, mBrowserLocation);
	}

	public void playYoutube(String youtubeURL) throws RemoteException, PlayerException {
		mWebMediaServer.playYoutube(mMediaServerID, mPlayer, youtubeURL);
	}

	public BeanDownload publishForDownload(String file) throws RemoteException, IOException {
		return mWebMediaServer.publishForDownload(mMediaServerID, file);
	}

}
