package de.remote.mediaserver.impl;

import java.io.File;
import java.io.IOException;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.mediaserver.api.PlayerException;

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

	@Override
	public void play(String file) {
		try {
			Runtime.getRuntime().exec(new String[] { PLAY, file });
			informFile(new File(file));
		} catch (IOException e) {
		}
		super.play(file);
	}

	@Override
	public void playPause() throws PlayerException {
		try {
			Runtime.getRuntime().exec(PLAY_PAUSE);
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.playPause();
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
	public void next() throws PlayerException {
		try {
			Runtime.getRuntime().exec(NEXT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.next();
	}

	@Override
	public void previous() throws PlayerException {
		try {
			Runtime.getRuntime().exec(PREVIOUS);
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.previous();
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
	public void fullScreen(boolean full) {
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
	public void setPlayingPosition(int second) throws RemoteException,
			PlayerException {
		throw new PlayerException("not supported function for totem");		
	}
}
