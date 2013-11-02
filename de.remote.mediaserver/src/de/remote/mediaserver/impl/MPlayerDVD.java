package de.remote.mediaserver.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.mediaserver.api.IDVDPlayer;
import de.remote.mediaserver.api.PlayerException;
import de.remote.mediaserver.api.PlayingBean;

public class MPlayerDVD extends MPlayer implements IDVDPlayer {

	public static enum ObserverState {
		LOADING, PLAYING, ERROR, DOWN
	}

	private PlayerObserver playerObserver;

	public MPlayerDVD(String playListfolder) {
		super(playListfolder);
	}

	@Override
	public void playDVD() throws RemoteException, PlayerException {
		try {
			quit();
		} catch (Exception e) {
		}
		try {
			String[] args = new String[] { "/usr/bin/mplayer", "dvdnav://",
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
			fullScreen(true);
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
		showMenu();
	}

	public static void main(String[] args) {
		MPlayerDVD mp = new MPlayerDVD("/home/sebastian/temp/playlists/");
		try {
			mp.playDVD();
			System.out.println("======================================");
			System.out.println("=============== dvd loaded ===========");
			System.out.println("======================================");
			Thread.sleep(10000);
			mp.showMenu();
			System.out.println("======================================");
			System.out.println("================== MENU ==============");
			System.out.println("======================================");
			Thread.sleep(1000);
			mp.menuDown();
			System.out.println("======================================");
			System.out.println("================== DOWN ==============");
			System.out.println("======================================");
			Thread.sleep(1000);
			mp.menuDown();
			System.out.println("======================================");
			System.out.println("================== DOWN ==============");
			System.out.println("======================================");
			Thread.sleep(1000);
			mp.menuDown();
			System.out.println("======================================");
			System.out.println("================== DOWN ==============");
			System.out.println("======================================");
			Thread.sleep(1000);
			mp.menuEnter();
			System.out.println("======================================");
			System.out.println("================== ENTER =============");
			System.out.println("======================================");
			System.out.println("dvd loaded");
			while (true) {
				Thread.sleep(2000);
				mp.nextAudio();
				mp.volUp();
				Thread.sleep(4000);
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

	@Override
	public void showMenu() throws RemoteException, PlayerException {
		writeCommand("dvdnav menu");
	}

	@Override
	public void menuUp() throws RemoteException, PlayerException {
		writeCommand("dvdnav up");
	}

	@Override
	public void menuDown() throws RemoteException, PlayerException {
		writeCommand("dvdnav down");
	}

	@Override
	public void menuLeft() throws RemoteException, PlayerException {
		writeCommand("dvdnav left");
	}

	@Override
	public void menuRight() throws RemoteException, PlayerException {
		writeCommand("dvdnav right");
	}

	@Override
	public void menuEnter() throws RemoteException, PlayerException {
		writeCommand("dvdnav select");
		fullScreen(true);
	}

	@Override
	public void menuPrevious() throws RemoteException, PlayerException {
		writeCommand("dvdnav prev");
	}

}
