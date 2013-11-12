package de.neo.remote.mediaserver.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import de.neo.remote.mediaserver.api.PlayerException;
import de.neo.remote.mediaserver.api.PlayingBean;
import de.neo.rmi.protokol.RemoteException;

public class OMXPlayer extends AbstractPlayer {

	public static final char ESCAPE = 0x1B;
	public static final String ARROW_UP = ESCAPE + "[A";
	public static final String ARROW_DOWN = ESCAPE + "[B";
	public static final String ARROW_LEFT = ESCAPE + "[D";
	public static final String ARROW_RIGHT = ESCAPE + "[C";

	protected Process omxProcess;
	protected PrintStream omxIn;
	protected OMXObserver omxObserver;
	private long lastSeekBack;
	private long lastSeekForeward;

	protected void writeCommand(String cmd) throws PlayerException {
		if (omxIn == null)
			throw new PlayerException("omxplayer is down");
		omxIn.print(cmd);
		omxIn.flush();
	}

	@Override
	public void play(String file) {
		try {
			quit();
		} catch (PlayerException e1) {
		}
		try {
			String[] args = new String[] { "/usr/bin/omxplayer", file };
			omxProcess = Runtime.getRuntime().exec(args);
			// the standard input of MPlayer
			omxIn = new PrintStream(omxProcess.getOutputStream());
			// start player observer
			new OMXObserver(omxProcess.getInputStream()).start();
			// set default volume
		} catch (IOException e) {
		}
		super.play(file);
	}

	@Override
	public void playPause() throws PlayerException {
		writeCommand("p");
		super.playPause();
	}

	@Override
	public void seekForwards() throws RemoteException, PlayerException {
		if (System.currentTimeMillis() - lastSeekForeward > 300)
			writeCommand(ARROW_RIGHT);
		else
			writeCommand(ARROW_UP);
		lastSeekForeward = System.currentTimeMillis();
	}

	@Override
	public void seekBackwards() throws RemoteException, PlayerException {
		if (System.currentTimeMillis() - lastSeekBack > 300)
			writeCommand(ARROW_LEFT);
		else
			writeCommand(ARROW_DOWN);
		lastSeekBack = System.currentTimeMillis();
	}

	@Override
	public void volUp() throws RemoteException, PlayerException {
		writeCommand("+");
	}

	@Override
	public void volDown() throws RemoteException, PlayerException {
		writeCommand("-");
	}

	@Override
	public void fullScreen(boolean full) throws RemoteException,
			PlayerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void nextAudio() throws RemoteException, PlayerException {
		writeCommand("k");
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
	public void useShuffle(boolean shuffle) throws RemoteException,
			PlayerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void playPlayList(String pls) throws RemoteException,
			PlayerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPlayingPosition(int second) throws RemoteException,
			PlayerException {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void playFromYoutube(String url) throws RemoteException,
			PlayerException {
		try {
			quit();
		} catch (PlayerException e1) {
		}
		try {
			String tempUrl = getYoutubeStreamUrl(url);
			String[] args = new String[] { "/usr/bin/omxplayer", tempUrl };
			omxProcess = Runtime.getRuntime().exec(args);
			// the standard input of MPlayer
			omxIn = new PrintStream(omxProcess.getOutputStream());
			// start player observer
			omxObserver = new OMXObserver(omxProcess.getInputStream());
			omxObserver.start();
			// set default volume
		} catch (IOException e) {
			throw new PlayerException("Could not play youtube: " + e.getMessage());
		}
	}

	@Override
	public void quit() throws PlayerException {
		writeCommand("q");
		super.quit();
	}

	private class OMXObserver extends Thread {

		private BufferedReader omxStream;

		public OMXObserver(InputStream omxStream) {
			this.omxStream = new BufferedReader(
					new InputStreamReader(omxStream));
		}

		@Override
		public void run() {
			String line = null;
			PlayingBean bean = new PlayingBean();
			try {
				while ((line = omxStream.readLine()) != null) {
					if (line.startsWith("have a nice day ;)")){
						bean.setState(PlayingBean.STATE.DOWN);
						informPlayingBean(bean);
					}
					if (line.startsWith("Audio codec")){
						bean.setState(PlayingBean.STATE.PLAY);
						informPlayingBean(bean);
					}
						
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
