package de.neo.remote.mediaserver.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.AbstractID3v2;

import de.neo.remote.mediaserver.api.IPlayer;
import de.neo.remote.mediaserver.api.IPlayerListener;
import de.neo.remote.mediaserver.api.PlayerException;
import de.neo.remote.mediaserver.api.PlayingBean;
import de.neo.remote.mediaserver.api.PlayingBean.STATE;
import de.neo.rmi.protokol.RemoteException;

/**
 * the abstract player implements basically functions of an player. this
 * contains handling of listeners, current playing file
 * 
 * @author sebastian
 */
public abstract class AbstractPlayer implements IPlayer {

	public static final String TATORT_DL_FILE = "/usr/bin/tatort-dl.sh";

	public static final String TATORT_TMP_FILE = "tatort_tmp.f4v";

	/**
	 * list of all listeners
	 */
	private List<IPlayerListener> listeners = new ArrayList<IPlayerListener>();

	/**
	 * current playing file
	 */
	protected PlayingBean playingBean;

	private String tempFolder;

	private Process tatortProcess;

	private String tatortURL;

	public AbstractPlayer(String tempFolder) {
		new PlayingTimeCounter().start();
		this.tempFolder = tempFolder;
		if (!this.tempFolder.endsWith(File.separator))
			this.tempFolder += File.separator;
	}

	@Override
	public void addPlayerMessageListener(IPlayerListener listener)
			throws RemoteException {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	@Override
	public void removePlayerMessageListener(IPlayerListener listener)
			throws RemoteException {
		listeners.remove(listener);
	}

	/**
	 * read file tags with the ID3 library
	 * 
	 * @param file
	 * @return playingbean
	 * @throws IOException
	 */
	protected PlayingBean readFileInformations(File file) throws IOException {
		PlayingBean bean = new PlayingBean();
		try {
			MP3File mp3File = new MP3File(file);
			bean.setFile(file.getName().trim());
			bean.setPath(file.getPath());
			AbstractID3v2 id3v2Tag = mp3File.getID3v2Tag();
			if (id3v2Tag != null) {
				if (id3v2Tag.getAuthorComposer() != null)
					bean.setArtist(id3v2Tag.getAuthorComposer().trim());
				if (id3v2Tag.getSongTitle() != null)
					bean.setTitle(id3v2Tag.getSongTitle().trim());
				if (id3v2Tag.getAlbumTitle() != null)
					bean.setAlbum(id3v2Tag.getAlbumTitle().trim());
			}
		} catch (TagException e) {
			System.out.println(e);
		}
		return bean;
	}

	/**
	 * inform all listeners about the current playing file. the information
	 * about the file will be read.
	 * 
	 * @param bean
	 * @throws IOException
	 */
	protected void informFile(File file) throws IOException {
		PlayingBean bean = readFileInformations(file);
		bean.setState(STATE.PLAY);
		informPlayingBean(bean);
	}

	/**
	 * inform all listeners about the current playing file. a new bean will be
	 * created.
	 * 
	 * @param bean
	 */
	protected void informPlayingBean(PlayingBean bean) {
		this.playingBean = new PlayingBean(bean);
		List<IPlayerListener> exceptionList = new ArrayList<IPlayerListener>();
		for (IPlayerListener listener : listeners)
			try {
				listener.playerMessage(playingBean);
			} catch (RemoteException e) {
				exceptionList.add(listener);
			}
		listeners.removeAll(exceptionList);
	}

	@Override
	public PlayingBean getPlayingBean() throws RemoteException, PlayerException {
		return playingBean;
	}

	@Override
	public void playPause() throws PlayerException {
		if (playingBean != null) {
			playingBean
					.setState((playingBean.getState() == STATE.PLAY) ? STATE.PAUSE
							: STATE.PLAY);
			informPlayingBean(playingBean);
		}
	}

	protected String getYoutubeStreamUrl(String url) throws RemoteException {
		try {
			String[] youtubeArgs = new String[] { "/usr/bin/youtube-dl", "-g",
					url };
			Process youtube = Runtime.getRuntime().exec(youtubeArgs);
			InputStreamReader input = new InputStreamReader(
					youtube.getInputStream());
			BufferedReader reader = new BufferedReader(input);
			return reader.readLine();
		} catch (IOException e) {
			throw new RemoteException("youtube", "Error play youtube stream :"
					+ e.getMessage());
		}
	}

	protected String openTemporaryArdFile(String url) throws RemoteException {
		if (tatortURL != null && tatortURL.equals(url))
			return tempFolder + TATORT_TMP_FILE;
		try {
			String tempFile = tempFolder + TATORT_TMP_FILE;
			File file = new File(tempFile);
			if (tatortProcess != null)
				tatortProcess.destroy();
			if (file.exists())
				file.delete();
			if (!new File(TATORT_DL_FILE).exists())
				throw new IllegalStateException("Missing script: "
						+ TATORT_DL_FILE);
			List<String> tatortArgs = new ArrayList<String>();
			tatortArgs.add(TATORT_DL_FILE);
			tatortArgs.add(url);
			tatortArgs.add(tempFile);
			ProcessBuilder pb = new ProcessBuilder(tatortArgs);
			tatortProcess = pb.start();
			InputStreamReader input = new InputStreamReader(
					tatortProcess.getInputStream());
			BufferedReader reader = new BufferedReader(input);
			String line = null;
			System.out.println("-> read request");
			while ((line = reader.readLine()) != null) {
				System.out.println("-> read line: " + line);
				if (line.contains("Saving to")) {
					Thread.sleep(2000);
					tatortURL = tempFile;
					return tempFile;
				}
			}
			input = new InputStreamReader(tatortProcess.getErrorStream());
			reader = new BufferedReader(input);
			System.out.println("-> read error request");
			while ((line = reader.readLine()) != null) {
				System.out.println("-> read line: " + line);
				if (line.contains("Error"))
					throw new RemoteException("", "Stream ARD: " + line);
			}
			System.out.println("-> end of request");
			throw new RemoteException("", "Stream ARD: not Connected");
		} catch (Exception e) {
			throw new RemoteException("youtube", "Error play youtube stream :"
					+ e.getMessage());
		}
	}

	@Override
	public void play(String file) {
		if (playingBean != null) {
			playingBean.setState(STATE.PLAY);
			playingBean.setFile(file);
			playingBean.setCurrentTime(0);
			informPlayingBean(playingBean);
		}
	}

	@Override
	public void quit() throws PlayerException {
		if (playingBean == null)
			playingBean = new PlayingBean();
		playingBean.setState(STATE.DOWN);
		informPlayingBean(playingBean);
	}

	@Override
	public void next() throws PlayerException {
		if (playingBean != null) {
			playingBean.setState(STATE.PLAY);
			informPlayingBean(playingBean);
		}
	}

	@Override
	public void previous() throws PlayerException {
		if (playingBean != null) {
			playingBean.setState(STATE.PLAY);
			informPlayingBean(playingBean);
		}
	}

	@Override
	public void playFromYoutube(String url) throws RemoteException,
			PlayerException {
	}

	@Override
	public void playFromArdMediathek(String url) throws RemoteException,
			PlayerException {

	}

	class PlayingTimeCounter extends Thread {

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (playingBean != null && playingBean.getState() == STATE.PLAY) {
					playingBean.incrementCurrentTime(1);
				}
			}
		}

	}
}
