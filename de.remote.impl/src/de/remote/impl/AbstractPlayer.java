package de.remote.impl;

import java.util.ArrayList;
import java.util.List;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IPlayer;
import de.remote.api.IPlayerListener;
import de.remote.api.PlayerException;
import de.remote.api.PlayingBean;

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
	private PlayingBean playingBean;

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
	 * inform all listeners about the current playing file.
	 * 
	 * @param bean
	 */
	protected void inform(PlayingBean bean) {
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
	public PlayingBean getPlayingFile() throws RemoteException, PlayerException {
		if (playingBean == null)
			return null;
		if (playingBean.getAlbum() == null && playingBean.getArtist() == null
				&& playingBean.getFile() == null
				&& playingBean.getRadio() == null
				&& playingBean.getTitle() == null)
			return null;
		return playingBean;
	}
}
