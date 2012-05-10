package de.remote.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.PlayerException;
import de.remote.api.PlayingBean;

public class MPlayer extends AbstractPlayer {

	private Process mplayerProcess;
	private PrintStream mplayerIn;
	private int fullscreen = 0;
	private boolean shuffle = false;
	private int volume = 50;
	private int seekValue;

	@Override
	public void play(String file) {
		if (mplayerProcess == null)
			startPlayer();

		if (new File(file).isDirectory()) {
			createPlayList(file);
			mplayerIn.print("loadlist /home/sebastian/temp/playlist.pls\n");
			mplayerIn.flush();
		} else {
			mplayerIn.print("loadfile \"" + file + "\" 0\n");
			mplayerIn.flush();
		}
		super.play(file);
	}

	private void createPlayList(String file) {
		try {
			Process exec = Runtime.getRuntime().exec(
					new String[] { "/usr/bin/find", file + "/" });
			PrintStream output = new PrintStream(new FileOutputStream(
					"/home/sebastian/temp/playlist.pls"));
			BufferedReader input = new BufferedReader(new InputStreamReader(
					exec.getInputStream()));
			BufferedReader error = new BufferedReader(new InputStreamReader(
					exec.getErrorStream()));
			String line = "";
			while ((line = input.readLine()) != null)
				output.println(line);
			while ((line = error.readLine()) != null)
				System.out.println(line);
			output.close();
			input.close();
			error.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startPlayer() {
		try {
			String[] args = null;
			if (shuffle)
				args = new String[] { "/usr/bin/mplayer", "-slave", "-quiet",
						"-idle", "-shuffle" };
			else
				args = new String[] { "/usr/bin/mplayer", "-slave", "-quiet",
						"-idle" };
			mplayerProcess = Runtime.getRuntime().exec(args);
			// the standard input of MPlayer
			mplayerIn = new PrintStream(mplayerProcess.getOutputStream());
			// start player observer
			new PlayerObserver(mplayerProcess.getInputStream()).start();
			// set default volume
			mplayerIn.print("volume " + volume + " 1\n");
			mplayerIn.flush();
		} catch (IOException e) {
			// throw new PlayerException(e.getMessage());
		}
	}

	@Override
	public void playPause() throws PlayerException {
		if (mplayerIn == null)
			throw new PlayerException("mplayer is down");
		mplayerIn.print("pause");
		mplayerIn.print("\n");
		mplayerIn.flush();
		super.playPause();
	}

	@Override
	public void quit() throws PlayerException {
		if (mplayerIn == null)
			throw new PlayerException("mplayer is down");
		mplayerIn.print("quit");
		mplayerIn.print("\n");
		mplayerIn.flush();
		mplayerIn = null;
		mplayerProcess = null;
		super.quit();
	}

	@Override
	public void next() throws PlayerException {
		if (mplayerIn == null)
			throw new PlayerException("mplayer is down");
		mplayerIn.print("pt_step 1");
		mplayerIn.print("\n");
		mplayerIn.flush();
		super.next();
	}

	@Override
	public void previous() throws PlayerException {
		if (mplayerIn == null)
			throw new PlayerException("mplayer is down");
		mplayerIn.print("pt_step -1");
		mplayerIn.print("\n");
		mplayerIn.flush();
		super.previous();
	}

	@Override
	public void seekForwards() throws RemoteException, PlayerException {
		if (mplayerIn == null)
			throw new PlayerException("mplayer is down");
		if (seekValue <= 0)
			seekValue = 5;
		else if (seekValue < -600)
			seekValue *= 5;
		mplayerIn.print("seek " + seekValue + " 0");
		mplayerIn.print("\n");
		mplayerIn.flush();
	}

	@Override
	public void seekBackwards() throws RemoteException, PlayerException {
		if (mplayerIn == null)
			throw new PlayerException("mplayer is down");
		if (seekValue >= 0)
			seekValue = -5;
		else if (seekValue > -600)
			seekValue *= 5;
		mplayerIn.print("seek " + seekValue + " 0");
		mplayerIn.print("\n");
		mplayerIn.flush();
	}

	@Override
	public void volUp() throws PlayerException {
		if (mplayerIn == null)
			throw new PlayerException("mplayer is down");
		volume += 7;
		if (volume > 100)
			volume = 100;
		mplayerIn.print("volume " + volume + " 1");
		mplayerIn.print("\n");
		mplayerIn.flush();
	}

	@Override
	public void volDown() throws PlayerException {
		if (mplayerIn == null)
			throw new PlayerException("mplayer is down");
		volume -= 7;
		if (volume < 0)
			volume = 0;
		mplayerIn.print("volume " + volume + " 1");
		mplayerIn.print("\n");
		mplayerIn.flush();
	}

	@Override
	public void fullScreen() throws PlayerException {
		if (mplayerIn == null)
			throw new PlayerException("mplayer is down");
		mplayerIn.print("vo_fullscreen " + (fullscreen++) % 2);
		mplayerIn.print("\n");
		mplayerIn.flush();
	}

	@Override
	public void nextAudio() throws PlayerException {
		if (mplayerIn == null)
			throw new PlayerException("mplayer is down");
		mplayerIn.print("switch_audio -1");
		mplayerIn.print("\n");
		mplayerIn.flush();
	}

	@Override
	public void moveLeft() throws PlayerException {
		if (mplayerIn == null)
			throw new PlayerException("mplayer is down");
		mplayerIn.print("change_rectangle 2 -800");
		mplayerIn.print("\n");
		mplayerIn.flush();
	}

	@Override
	public void moveRight() throws PlayerException {
		if (mplayerIn == null)
			throw new PlayerException("mplayer is down");
		mplayerIn.print("change_rectangle 2 800");
		mplayerIn.print("\n");
		mplayerIn.flush();
	}

	@Override
	public void playPlayList(String pls) throws RemoteException,
			PlayerException {
		if (mplayerIn == null) {
			startPlayer();
		}
		if (!new File(pls).exists())
			throw new PlayerException("playlist " + pls + " does not exist");
		mplayerIn.print("loadlist " + pls + "\n");
		mplayerIn.flush();
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
					if (line.startsWith("Playing")){
						try {
							String file = line.substring(8);
							file = file.substring(0,file.length()-1);
							bean = readFileInformations(new File(file));
						} catch (IOException e) {
							bean = new PlayingBean();
						}
						String file = line.substring(line.lastIndexOf(File.separator)+1);
						file = file.substring(0, file.length()-1);
						bean.setFile(file.trim());
					}
					if (line.startsWith(" Title: "))
						bean.setTitle(line.substring(8).trim());
					if (line.startsWith(" Artist: "))
						bean.setArtist(line.substring(9).trim());
					if (line.startsWith(" Album: "))
						bean.setAlbum(line.substring(8).trim());
					if (line.equals("Starting playback...")) {
						bean.setState(PlayingBean.STATE.PLAY);
						informPlayingBean(bean);
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
	public void useShuffle(boolean shuffle) throws RemoteException,
			PlayerException {
		this.shuffle = shuffle;
		try {
			quit();
		} catch (Exception e) {

		}
		startPlayer();
	}
}
