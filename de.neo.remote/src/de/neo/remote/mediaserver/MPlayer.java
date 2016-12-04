package de.neo.remote.mediaserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import de.neo.remote.RemoteLogger;
import de.neo.remote.api.PlayerException;
import de.neo.remote.api.PlayingBean;
import de.neo.remote.api.PlayingBean.STATE;
import de.neo.rmi.api.RMILogger.LogPriority;
import de.neo.rmi.protokol.RemoteException;

public class MPlayer extends AbstractPlayer {

	protected Process mMplayerProcess;
	protected PrintStream mMplayerIn;
	protected int mPositionLeft = 0;
	private int mSeekValue;
	private Object mPlayListfolder;

	public MPlayer(String playListfolder) {
		this.mPlayListfolder = playListfolder;
	}

	protected void writeCommand(String cmd) throws PlayerException {
		if (mMplayerIn == null)
			throw new PlayerException("mplayer is down");
		mMplayerIn.print(cmd);
		mMplayerIn.print("\n");
		mMplayerIn.flush();
	}

	@Override
	public void play(String file) {
		if (mMplayerProcess == null)
			startPlayer();

		if (new File(file).isDirectory()) {
			createPlayList(file);
			mMplayerIn.print("loadlist " + mPlayListfolder + "/playlist.pls\n");
			mMplayerIn.flush();
		} else {
			mMplayerIn.print("loadfile \"" + file + "\" 0\n");
			mMplayerIn.flush();
		}
		try {
			writeVolume();
		} catch (PlayerException e) {
		}
		super.play(file);
	}

	private void createPlayList(String file) {
		try {
			Process exec = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", "find \"" + file + "/\" | sort" });
			PrintStream output = new PrintStream(new FileOutputStream(mPlayListfolder + "/playlist.pls"));
			BufferedReader input = new BufferedReader(new InputStreamReader(exec.getInputStream()));
			BufferedReader error = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
			String line = "";
			while ((line = input.readLine()) != null)
				output.println(line);
			while ((line = error.readLine()) != null)
				RemoteLogger.performLog(LogPriority.ERROR, "Error creating playlist: " + line, "Mediaserver");
			output.close();
			input.close();
			error.close();
		} catch (IOException e) {
			RemoteLogger.performLog(LogPriority.ERROR, e.getClass().getSimpleName() + ": " + e.getMessage(), "MPlayer");
		}
	}

	protected void startPlayer() {
		try {
			String[] args = new String[] { "/usr/bin/mplayer", "-slave", "-quiet", "-idle", "-geometry",
					mPositionLeft + ":0" };
			mMplayerProcess = Runtime.getRuntime().exec(args);
			// the standard input of MPlayer
			mMplayerIn = new PrintStream(mMplayerProcess.getOutputStream());
			// start player observer
			new PlayerObserver(mMplayerProcess.getInputStream()).start();
			// set default volume
			mMplayerIn.print("volume " + mVolume + " 1\n");
			mMplayerIn.flush();
			// wait for mplayer to get the new volume
			Thread.sleep(200);
		} catch (IOException e) {
			// throw new PlayerException(e.getMessage());
		} catch (InterruptedException e) {
			// ignore
		}
	}

	@Override
	public void playPause() throws PlayerException {
		writeCommand("pause");
		super.playPause();
	}

	@Override
	public void quit() throws PlayerException {
		writeCommand("quit");
		mMplayerIn = null;
		mMplayerProcess = null;
		super.quit();
	}

	@Override
	public void next() throws PlayerException {
		writeCommand("pt_step 1");
		super.next();
	}

	@Override
	public void previous() throws PlayerException {
		writeCommand("pt_step -1");
		super.previous();
	}

	@Override
	public void seekForwards() throws RemoteException, PlayerException {
		if (mSeekValue <= 0)
			mSeekValue = 5;
		else if (mSeekValue < -600)
			mSeekValue *= 5;
		writeCommand("seek " + mSeekValue + " 0");
		mPlayingBean.incrementCurrentTime(mSeekValue);
	}

	@Override
	public void seekBackwards() throws RemoteException, PlayerException {
		if (mSeekValue >= 0)
			mSeekValue = -5;
		else if (mSeekValue > -600)
			mSeekValue *= 5;
		writeCommand("seek " + mSeekValue + " 0");
		mPlayingBean.incrementCurrentTime(mSeekValue);
	}

	@Override
	public void volUp() throws PlayerException {
		mVolume += 3;
		if (mVolume > 100)
			mVolume = 100;
		writeVolume();
	}

	@Override
	public void volDown() throws PlayerException {
		mVolume -= 3;
		if (mVolume < 0)
			mVolume = 0;
		writeVolume();
	}

	private void writeVolume() throws PlayerException {
		writeCommand("volume " + mVolume + " 1");
	}

	@Override
	public void fullScreen(boolean full) throws PlayerException {
		if (full)
			writeCommand("vo_fullscreen 1");
		else
			writeCommand("vo_fullscreen 0");
	}

	@Override
	public void nextAudio() throws PlayerException {
		writeCommand("switch_audio -1");
	}

	@Override
	public void moveLeft() throws PlayerException {
		long time = 0;
		if (mPlayingBean != null)
			time = Math.max(System.currentTimeMillis() - mPlayingBean.getStartTime() - 3, 0);
		quit();
		mPositionLeft -= 1680;
		startPlayer();
		if (mPlayingBean != null && mPlayingBean.getPath() != null) {
			play(mPlayingBean.getPath());
			try {
				setPlayingPosition((int) time);
			} catch (RemoteException e) {
				RemoteLogger.performLog(LogPriority.ERROR, e.getClass().getSimpleName() + ": " + e.getMessage(),
						"MPlayer");
			}
		}
	}

	@Override
	public void playFromYoutube(String url) throws RemoteException, PlayerException {
		if (mMplayerIn == null)
			startPlayer();
		String[] split = url.split(" ");
		String title = "";
		for (int i = 0; i < split.length - 1; i++)
			title = title + " " + split[i];
		String youtubeStreamUrl = getStreamUrl(YOUTUBE_DL_FILE, split[split.length - 1]);
		writeCommand("loadfile " + youtubeStreamUrl);
	}

	@Override
	public void moveRight() throws PlayerException {
		long time = 0;
		if (mPlayingBean != null)
			time = Math.max(System.currentTimeMillis() - mPlayingBean.getStartTime() - 3, 0);
		quit();
		mPositionLeft += 1680;
		startPlayer();
		if (mPlayingBean != null && mPlayingBean.getPath() != null) {
			play(mPlayingBean.getPath());
			try {
				setPlayingPosition((int) time);
			} catch (RemoteException e) {
				RemoteLogger.performLog(LogPriority.ERROR, e.getClass().getSimpleName() + ": " + e.getMessage(),
						"MPlayer");
			}
		}
	}

	@Override
	public void playPlayList(String pls) throws RemoteException, PlayerException {
		if (mMplayerIn == null) {
			startPlayer();
		}
		if (!new File(pls).exists())
			throw new PlayerException("playlist " + pls + " does not exist");

		if (lineOfFileStartsWith(pls, "[playlist]") != null) {
			String url = lineOfFileStartsWith(pls, "File");
			url = url.substring(url.indexOf("=") + 1);
			mMplayerIn.print("loadfile " + url + "\n");
		} else
			mMplayerIn.print("loadlist \"" + pls + "\"\n");

		writeVolume();
	}

	private String lineOfFileStartsWith(String file, String prefix) {
		String match = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(file)));
			String line = null;
			while ((line = reader.readLine()) != null)
				if (line.startsWith(prefix)) {
					match = line;
					break;
				}
			reader.close();
		} catch (IOException e) {
			RemoteLogger.performLog(LogPriority.ERROR, e.getClass().getSimpleName() + ": " + e.getMessage(), "MPlayer");
		}
		return match;
	}

	class PlayerObserver extends Thread {
		private BufferedReader input;

		public PlayerObserver(InputStream stream) {
			input = new BufferedReader(new InputStreamReader(stream));
		}

		@Override
		public void run() {
			String line = null;
			PlayingBean bean = new PlayingBean();
			try {
				while ((line = input.readLine()) != null) {
					if (line.startsWith("Playing")) {
						try {
							String file = line.substring(8);
							file = file.substring(0, file.length() - 1);
							bean = readFileInformations(new File(file));
						} catch (IOException e) {
							bean = new PlayingBean();
						}
						String file = line.substring(line.lastIndexOf(File.separator) + 1);
						file = file.substring(0, file.length() - 1);
						bean.setFile(file.trim());
						if (mPlayingBean != null && mPlayingBean.getPath() != null
								&& mPlayingBean.getPath().equals(bean.getPath()))
							bean.setStartTime(mPlayingBean.getStartTime());
					}
					if (line.startsWith(" Title: "))
						bean.setTitle(line.substring(8).trim());
					if (line.startsWith(" Artist: "))
						bean.setArtist(line.substring(9).trim());
					if (line.startsWith(" Album: "))
						bean.setAlbum(line.substring(8).trim());
					if (line.equals("Starting playback...")) {
						bean.setState(PlayingBean.STATE.PLAY);
						bean.setStartTime(System.currentTimeMillis());
						loadThumbnail(bean);
						informPlayingBean(bean);
					}
					if (line.startsWith("ICY Info")) {
						bean.setStartTime(System.currentTimeMillis());
						bean.parseICYInfo(line);
						bean.setState(STATE.PLAY);
						loadThumbnail(bean);
						informPlayingBean(bean);
					}
				}
				bean.setVolume(mVolume);
				bean.setState(PlayingBean.STATE.DOWN);
				informPlayingBean(bean);
			} catch (IOException e) {
				RemoteLogger.performLog(LogPriority.ERROR, e.getClass().getSimpleName() + ": " + e.getMessage(),
						"MPlayerListener");
			}
		}

	}

	@Override
	public void setPlayingPosition(int second) throws RemoteException, PlayerException {
		writeCommand("seek " + second + " 2");
	}

	@Override
	public void useShuffle(boolean shuffle) throws RemoteException, PlayerException {
		throw new PlayerException("shuffle is not supported jet.");
	}

	@Override
	public void setVolume(int volume) throws RemoteException, PlayerException {
		mVolume = volume;
		if (mPlayingBean != null)
			mPlayingBean.setVolume(volume);
		writeVolume();
	}
}
