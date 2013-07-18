package de.remote.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.AbstractID3v2;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IPlayer;
import de.remote.api.IPlayerListener;
import de.remote.api.PlayerException;
import de.remote.api.PlayingBean;
import de.remote.api.PlayingBean.STATE;

/**
 * the abstract player implements basically functions of an player. this
 * contains handling of listeners, current playing file
 * 
 * @author sebastian
 */
public abstract class AbstractPlayer implements IPlayer {

	/**
	 * list of all listeners
	 */
	private List<IPlayerListener> listeners = new ArrayList<IPlayerListener>();

	/**
	 * current playing file
	 */
	protected PlayingBean playingBean;

	public AbstractPlayer() {
		new PlayingTimeCounter().start();
	}

	@Override
	public void fullScreen() throws RemoteException, PlayerException {

	}

	@Override
	public void addPlayerMessageListener(IPlayerListener listener)
			throws RemoteException {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	@Override
	public void removePlayerMessageListener(IPlayerListener listener)
			throws RemoteException {
		listeners.remove(listener);
	}

	/**
	 * read file tags with the ID3 library
	 * 
	 * @param file
	 * @return playingbean
	 * @throws IOException
	 */
	protected PlayingBean readFileInformations(File file) throws IOException {
		PlayingBean bean = new PlayingBean();
		try {
			MP3File mp3File = new MP3File(file);
			bean.setFile(file.getName().trim());
			bean.setPath(file.getPath());
			AbstractID3v2 id3v2Tag = mp3File.getID3v2Tag();
			if (id3v2Tag != null) {
				if (id3v2Tag.getAuthorComposer() != null)
					bean.setArtist(id3v2Tag.getAuthorComposer().trim());
				if (id3v2Tag.getSongTitle() != null)
					bean.setTitle(id3v2Tag.getSongTitle().trim());
				if (id3v2Tag.getAlbumTitle() != null)
					bean.setAlbum(id3v2Tag.getAlbumTitle().trim());
			}
		} catch (TagException e) {
			System.out.println(e);
		}
		return bean;
	}

	/**
	 * inform all listeners about the current playing file. the information
	 * about the file will be read.
	 * 
	 * @param bean
	 * @throws IOException
	 */
	protected void informFile(File file) throws IOException {
		PlayingBean bean = readFileInformations(file);
		bean.setState(STATE.PLAY);
		informPlayingBean(bean);
	}

	/**
	 * inform all listeners about the current playing file. a new bean will be
	 * created.
	 * 
	 * @param bean
	 */
	protected void informPlayingBean(PlayingBean bean) {
		this.playingBean = new PlayingBean(bean);
		List<IPlayerListener> exceptionList = new ArrayList<IPlayerListener>();
		for (IPlayerListener listener : listeners)
			try {
				listener.playerMessage(playingBean);
			} catch (RemoteException e) {
				exceptionList.add(listener);
			}
		listeners.removeAll(exceptionList);
	}

	@Override
	public PlayingBean getPlayingBean() throws RemoteException, PlayerException {
		return playingBean;
	}

	@Override
	public void playPause() throws PlayerException {
		if (playingBean != null) {
			playingBean
					.setState((playingBean.getState() == STATE.PLAY) ? STATE.PAUSE
							: STATE.PLAY);
			informPlayingBean(playingBean);
		}
	}

	@Override
	public void play(String file) {
		if (playingBean != null) {
			playingBean.setState(STATE.PLAY);
			playingBean.setFile(file);
			playingBean.setCurrentTime(0);
			informPlayingBean(playingBean);
		}
	}

	@Override
	public void quit() throws PlayerException {
		if (playingBean == null)
			playingBean = new PlayingBean();
		playingBean.setState(STATE.DOWN);
		informPlayingBean(playingBean);
	}

	@Override
	public void next() throws PlayerException {
		if (playingBean != null) {
			playingBean.setState(STATE.PLAY);
			informPlayingBean(playingBean);
		}
	}

	@Override
	public void previous() throws PlayerException {
		if (playingBean != null) {
			playingBean.setState(STATE.PLAY);
			informPlayingBean(playingBean);
		}
	}

	class PlayingTimeCounter extends Thread {

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (playingBean != null && playingBean.getState() == STATE.PLAY) {
					playingBean.incrementCurrentTime(1);
				}
			}
		}

	}
}
