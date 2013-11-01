package de.remote.mediaserver.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.mediaserver.api.PlayerException;
import de.remote.mediaserver.api.PlayingBean;

public class MPlayerDVD extends MPlayer {

	public static enum ObserverState {
		LOADING, PLAYING, ERROR, DOWN
	}

	private PlayerObserver playerObserver;

	public MPlayerDVD(String playListfolder) {
		super(playListfolder);
	}
	
	@Override
	public void nextTitle() throws PlayerException {
		if (mplayerIn == null)
			throw new PlayerException("mplayer is down");
		mplayerIn.print("switch_title -1");
		mplayerIn.print("\n");
		mplayerIn.flush();
	}

	@Override
	public void playDVD() throws RemoteException, PlayerException {
		try {
			quit();
		} catch (Exception e) {
		}
		try {
			String[] args = new String[] { "/usr/bin/mplayer", "dvd://",
					"/dev/dvd", "-slave", "-geometry", positionLeft + ":0" };
			mplayerProcess = Runtime.getRuntime().exec(args);
			// the standard input of MPlayer
			mplayerIn = new PrintStream(mplayerProcess.getOutputStream());
			// start player observer
			playerObserver = new PlayerObserver(mplayerProcess.getInputStream());
			playerObserver.start();
			// set default volume
			mplayerIn.print("volume " + volume + " 1\n");
			mplayerIn.flush();
			fullscreen = 1;
		} catch (IOException e) {
		}
		while (playerObserver.state == ObserverState.LOADING) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
		}
		if (playerObserver.state == ObserverState.ERROR)
			throw new PlayerException(playerObserver.message);
		if (playerObserver.state == ObserverState.DOWN)
			throw new PlayerException("Player is down");
	}

	public static void main(String[] args) {
		MPlayer mp = new MPlayerDVD("/home/sebastian/temp/playlists/");
		try {
			mp.playDVD();
			Thread.sleep(4000);
			mp.nextTitle();
			System.out.println("dvd loaded");
			while (true) {
				Thread.sleep(2000);
				mp.nextAudio();
				mp.volUp();
				Thread.sleep(4000);
				mp.nextTitle();
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PlayerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	class PlayerObserver extends Thread {
		private BufferedReader input;
		private ObserverState state = ObserverState.LOADING;
		private String message;

		public PlayerObserver(InputStream stream) {
			input = new BufferedReader(new InputStreamReader(stream));
		}

		@Override
		public void run() {
			String line = null;
			PlayingBean bean = new PlayingBean();
			try {
				while ((line = input.readLine()) != null) {
					System.out.println(line);
					if (line.startsWith("Failed to open /dev/dvd")) {
						state = ObserverState.ERROR;
						message = "Failed to open DVD";
					}
					if (line.startsWith("Exiting... (End of file)")) {
						state = ObserverState.DOWN;
					}
					if (line.startsWith("Starting playback...")) {
						state = ObserverState.PLAYING;
					}
				}
				bean.setState(PlayingBean.STATE.DOWN);
				informPlayingBean(bean);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
