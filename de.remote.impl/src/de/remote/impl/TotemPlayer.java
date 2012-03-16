package de.remote.impl;

import java.io.IOException;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IPlayer;
import de.remote.api.IPlayerListener;
import de.remote.api.PlayerException;

public class TotemPlayer implements IPlayer {

	private static final String QUIT = "totem --quit";
	private static final String PLAY_PAUSE = "totem --play-pause";
	private static final String PLAY = "totem";
	private static final String NEXT = "totem --next";
	private static final String PREVIOUS = "totem --previous";
	private static final String VOL_UP = "totem --volume-up";
	private static final String VOL_DOWN = "totem --volume-down";
	private static final String FULL_SCREEN = "totem --fullscreen";

	@Override
	public void play(String file) {
		try {
			Runtime.getRuntime().exec(new String[] { PLAY, file });
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void playPause() {
		try {
			Runtime.getRuntime().exec(PLAY_PAUSE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void quit() {
		try {
			Runtime.getRuntime().exec(QUIT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void next() {
		try {
			Runtime.getRuntime().exec(NEXT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void previous() {
		try {
			Runtime.getRuntime().exec(PREVIOUS);
		} catch (IOException e) {
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
		// TODO Auto-generated method stub

	}

	@Override
	public void moveLeft() throws RemoteException, PlayerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveRight() throws RemoteException, PlayerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addPlayerMessageListener(IPlayerListener listener)
			throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePlayerMessageListener(IPlayerListener listener)
			throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void playPlayList(String pls) throws RemoteException,
			PlayerException {
		try {
			Runtime.getRuntime().exec(
					new String[] { PLAY,
							PlayListImpl.PLAYLIST_LOCATION + pls + ".pls" });
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
