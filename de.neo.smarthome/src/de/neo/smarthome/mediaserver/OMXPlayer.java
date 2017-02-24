package de.neo.smarthome.mediaserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import de.neo.remote.api.RMILogger.LogPriority;
import de.neo.remote.protokol.RemoteException;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.api.PlayerException;
import de.neo.smarthome.api.PlayingBean;
import de.neo.smarthome.api.PlayingBean.STATE;

public class OMXPlayer extends AbstractPlayer {

	public static final char ESCAPE = 0x1B;
	public static final String ARROW_UP = ESCAPE + "[A";
	public static final String ARROW_DOWN = ESCAPE + "[B";
	public static final String ARROW_LEFT = ESCAPE + "[D";
	public static final String ARROW_RIGHT = ESCAPE + "[C";

	protected Process mOmxProcess;
	protected PrintStream mOmxIn;
	protected OMXObserver mOmxObserver;
	private long mLastSeekBack;
	private long mLastSeekForeward;
	private int mCurrentVolume;

	public OMXPlayer() {
		mCurrentVolume = 0;
	}

	protected void writeCommand(String cmd) throws PlayerException {
		if (mOmxIn == null)
			throw new PlayerException("omxplayer is down");
		mOmxIn.print(cmd);
		mOmxIn.flush();
	}

	@Override
	public void play(String file) {
		try {
			quit();
		} catch (PlayerException e1) {
		}
		try {
			String[] args = new String[] { "/usr/bin/omxplayer", "-o", "local",
					file };
			mOmxProcess = Runtime.getRuntime().exec(args);
			// the standard input of MPlayer
			mOmxIn = new PrintStream(mOmxProcess.getOutputStream());
			// start player observer
			new OMXObserver(mOmxProcess.getInputStream()).start();
			informFile(new File(file));
		} catch (IOException e) {
		}
		super.play(file);
	}

	private void writeCurrentVolume() throws RemoteException, PlayerException {
		for (int i = 1; i <= mCurrentVolume; i++)
			writeCommand("+");
		for (int i = -1; i >= mCurrentVolume; i--)
			writeCommand("-");
	}

	@Override
	public void playPause() throws PlayerException {
		writeCommand("p");
		super.playPause();
	}

	@Override
	public void seekForwards() throws RemoteException, PlayerException {
		if (System.currentTimeMillis() - mLastSeekForeward > 300)
			writeCommand(ARROW_RIGHT);
		else
			writeCommand(ARROW_UP);
		mLastSeekForeward = System.currentTimeMillis();
	}

	@Override
	public void seekBackwards() throws RemoteException, PlayerException {
		if (System.currentTimeMillis() - mLastSeekBack > 300)
			writeCommand(ARROW_LEFT);
		else
			writeCommand(ARROW_DOWN);
		mLastSeekBack = System.currentTimeMillis();
	}

	@Override
	public void volUp() throws RemoteException, PlayerException {
		writeCommand("+");
		mCurrentVolume++;
	}

	@Override
	public void volDown() throws RemoteException, PlayerException {
		writeCommand("-");
		mCurrentVolume++;
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
		String[] split = url.split(" ");
		String title = "";
		for (int i = 0; i < split.length - 1; i++)
			title = title + " " + split[i];
		streamFromUrl(YOUTUBE_DL_FILE, split[split.length - 1], title);
	}

	public void streamFromUrl(String script, String url, String title)
			throws RemoteException, PlayerException {
		try {
			quit();
		} catch (PlayerException e1) {
		}
		try {
			String tempUrl = getStreamUrl(script, url);
			String[] args = new String[] { "/usr/bin/omxplayer", "-o", "local",
					tempUrl };
			mOmxProcess = Runtime.getRuntime().exec(args);
			// the standard input of MPlayer
			mOmxIn = new PrintStream(mOmxProcess.getOutputStream());
			// start player observer
			mOmxObserver = new OMXObserver(mOmxProcess.getInputStream());
			mOmxObserver.start();
			mPlayingBean = new PlayingBean();
			mPlayingBean.setPath(url);
			mPlayingBean.setTitle(title);
			mPlayingBean.setState(STATE.PLAY);
			informPlayingBean(mPlayingBean);
		} catch (IOException e) {
			throw new PlayerException("Could not stream url: " + e.getMessage());
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
					if (line.startsWith("have a nice day ;)")) {
						bean.setState(PlayingBean.STATE.DOWN);
						informPlayingBean(bean);
					}
					if (line.startsWith("Audio codec")) {
						bean.setState(PlayingBean.STATE.PLAY);
						try {
							writeCurrentVolume();
						} catch (RemoteException | PlayerException e) {
							RemoteLogger.performLog(
									LogPriority.ERROR,
									"error writing current value: "
											+ e.getMessage(), "omxplayer");
						}
						informPlayingBean(bean);
					}

				}
			} catch (IOException e) {
				RemoteLogger
						.performLog(LogPriority.ERROR, e.getClass()
								.getSimpleName() + ": " + e.getMessage(),
								"OMXListener");
			}
		}

	}

}
