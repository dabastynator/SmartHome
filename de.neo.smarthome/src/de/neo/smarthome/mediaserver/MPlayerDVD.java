package de.neo.smarthome.mediaserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import de.neo.remote.rmi.RemoteException;
import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.api.IDVDPlayer;
import de.neo.smarthome.api.PlayerException;
import de.neo.smarthome.api.PlayingBean;

public class MPlayerDVD extends MPlayer implements IDVDPlayer {

	public static enum ObserverState {
		LOADING, PLAYING, ERROR, DOWN
	}

	private PlayerObserver mPlayerObserver;

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
					"/dev/dvd", "-slave", "-geometry", mPositionLeft + ":0" };
			mMplayerProcess = Runtime.getRuntime().exec(args);
			// the standard input of MPlayer
			mMplayerIn = new PrintStream(mMplayerProcess.getOutputStream());
			// start player observer
			mPlayerObserver = new PlayerObserver(mMplayerProcess.getInputStream());
			mPlayerObserver.start();
			// set default volume
			mMplayerIn.print("volume " + mVolume + " 1\n");
			mMplayerIn.flush();
			fullScreen(true);
		} catch (IOException e) {
		}
		while (mPlayerObserver.state == ObserverState.LOADING) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
		}
		if (mPlayerObserver.state == ObserverState.ERROR)
			throw new PlayerException(mPlayerObserver.message);
		if (mPlayerObserver.state == ObserverState.DOWN)
			throw new PlayerException("Player is down");
		showMenu();
	}

	public static void main(String[] args) {
		MPlayerDVD mp = new MPlayerDVD("/home/sebastian/temp/playlists/");
		try {
			mp.playDVD();
			Thread.sleep(2000);
			System.out.println("enter");
			mp.menuEnter();
			Thread.sleep(2000);
			System.out.println("no fullscreen");
			mp.fullScreen(false);
			Thread.sleep(16000);
			System.out.println("remove subtitle");
			mp.subtitleRemove();
			Thread.sleep(4000);
			System.out.println("next subtitle");
			mp.subtitleNext();
			Thread.sleep(4000);
			System.out.println("next subtitle");
			mp.ejectDVD();
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
					// System.out.println(line);
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
				RemoteLogger.performLog(LogPriority.ERROR, e.getClass()
						.getSimpleName() + ": " + e.getMessage(),
						"MPlayerListener");
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

	@Override
	public void subtitleRemove() throws RemoteException, PlayerException {
		writeCommand("sub_select -1");
	}

	@Override
	public void subtitleNext() throws RemoteException, PlayerException {
		writeCommand("sub_select -2");
	}

	@Override
	public void ejectDVD() throws RemoteException {
		try {
			quit();
		} catch (PlayerException e) {
		}
		try {
			String[] args = new String[] { "/usr/bin/eject", "/dev/dvd" };
			mMplayerProcess = Runtime.getRuntime().exec(args);
		} catch (IOException e) {
		}

	}

}
