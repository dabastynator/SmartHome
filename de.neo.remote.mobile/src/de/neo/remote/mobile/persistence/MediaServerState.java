package de.neo.remote.mobile.persistence;

import java.util.ArrayList;

import de.neo.android.persistence.DomainBase;
import de.neo.android.persistence.Persistent;
import de.neo.remote.api.IWebMediaServer;
import de.neo.remote.api.IWebMediaServer.BeanMediaServer;
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

	public void initialize(IWebMediaServer webMediaServer) {
		mWebMediaServer = webMediaServer;
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

}
