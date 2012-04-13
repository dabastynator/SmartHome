package de.remote.impl;

import java.io.File;
import java.io.IOException;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.PlayerException;
import de.remote.api.PlayingBean;
import de.remote.api.PlayingBean.STATE;

public class TotemPlayer extends AbstractPlayer {

	private static final String QUIT = "totem --quit";
	private static final String PLAY_PAUSE = "totem --play-pause";
	private static final String PLAY = "totem";
	private static final String NEXT = "totem --next";
	private static final String PREVIOUS = "totem --previous";
	private static final String VOL_UP = "totem --volume-up";
	private static final String VOL_DOWN = "totem --volume-down";
	private static final String FULL_SCREEN = "totem --fullscreen";
	private static final String SEEK_FWD = "totem --seek-fwd";
	private static final String SEEK_BWD = "totem --seek-bwd";

	private STATE state;

	@Override
	public void play(String file) {
		try {
			state = STATE.PLAY;
			Runtime.getRuntime().exec(new String[] { PLAY, file });
			informFile(new File(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void playPause() {
		try {
			Runtime.getRuntime().exec(PLAY_PAUSE);
			state = (state == STATE.PLAY) ? STATE.PAUSE : STATE.PLAY;
			getPlayingBean().setState(state);
			informPlayingBean(getPlayingBean());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PlayerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void quit() {
		try {
			Runtime.getRuntime().exec(QUIT);
			getPlayingBean().setState(STATE.DOWN);
			informPlayingBean(getPlayingBean());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (PlayerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void next() {
		try {
			Runtime.getRuntime().exec(NEXT);
			getPlayingBean().setState(STATE.PLAY);
			state = STATE.PLAY;
			informPlayingBean(getPlayingBean());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PlayerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void previous() {
		try {
			Runtime.getRuntime().exec(PREVIOUS);
			getPlayingBean().setState(STATE.PLAY);
			state = STATE.PLAY;
			informPlayingBean(getPlayingBean());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (PlayerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void volUp() {
		try {
			Runtime.getRuntime().exec(VOL_UP);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void volDown() {
		try {
			Runtime.getRuntime().exec(VOL_DOWN);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void fullScreen() {
		try {
			Runtime.getRuntime().exec(FULL_SCREEN);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void nextAudio() throws RemoteException, PlayerException {
		throw new PlayerException("not supported function for totem");
	}

	@Override
	public void moveLeft() throws RemoteException, PlayerException {
		throw new PlayerException("not supported function for totem");
	}

	@Override
	public void moveRight() throws RemoteException, PlayerException {
		throw new PlayerException("not supported function for totem");
	}

	@Override
	public void playPlayList(String pls) throws RemoteException,
			PlayerException {
		try {
			Runtime.getRuntime().exec(new String[] { PLAY, pls });
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void seekForwards() throws RemoteException, PlayerException {
		try {
			Runtime.getRuntime().exec(SEEK_FWD);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void seekBackwards() throws RemoteException, PlayerException {
		try {
			Runtime.getRuntime().exec(SEEK_BWD);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void useShuffle(boolean shuffle) throws RemoteException,
			PlayerException {
		throw new PlayerException("not supported function for totem");
	}

	@Override
	public PlayingBean getPlayingBean() throws RemoteException, PlayerException {
		throw new PlayerException("not supported function for totem");
	}

}
