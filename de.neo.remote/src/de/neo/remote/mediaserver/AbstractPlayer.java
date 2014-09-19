package de.neo.remote.mediaserver;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.AbstractID3v2;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.neo.remote.RemoteLogger;
import de.neo.remote.api.IPlayer;
import de.neo.remote.api.IPlayerListener;
import de.neo.remote.api.PlayerException;
import de.neo.remote.api.PlayingBean;
import de.neo.remote.api.PlayingBean.STATE;
import de.neo.remote.mediaserver.ThumbnailHandler.Thumbnail;
import de.neo.remote.mediaserver.ThumbnailHandler.ThumbnailJob;
import de.neo.remote.mediaserver.ThumbnailHandler.ThumbnailListener;
import de.neo.rmi.api.RMILogger.LogPriority;
import de.neo.rmi.protokol.RemoteException;

/**
 * the abstract player implements basically functions of an player. this
 * contains handling of listeners, current playing file
 * 
 * @author sebastian
 */
public abstract class AbstractPlayer implements IPlayer, ThumbnailListener {

	public static final String TATORT_DL_FILE = "/usr/bin/tatort-dl.sh";

	public static final String YOUTUBE_DL_FILE = "/usr/bin/youtube-dl";

	public static final int THUMBNAIL_SIZE = 128;

	/**
	 * list of all listeners
	 */
	private List<IPlayerListener> mListeners = new ArrayList<IPlayerListener>();

	/**
	 * current playing file
	 */
	protected PlayingBean mPlayingBean;

	public AbstractPlayer() {
		new PlayingTimeCounter().start();
		ThumbnailHandler.instance().calculationListener().add(this);
	}

	@Override
	public void addPlayerMessageListener(IPlayerListener listener)
			throws RemoteException {
		if (!mListeners.contains(listener))
			mListeners.add(listener);
	}

	@Override
	public void removePlayerMessageListener(IPlayerListener listener)
			throws RemoteException {
		mListeners.remove(listener);
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
			RemoteLogger.performLog(LogPriority.ERROR,
					"Cant read file-information: " + e.getMessage(),
					"Mediaserver");
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
		this.mPlayingBean = new PlayingBean(bean);
		List<IPlayerListener> exceptionList = new ArrayList<IPlayerListener>();
		for (IPlayerListener listener : mListeners)
			try {
				listener.playerMessage(mPlayingBean);
			} catch (RemoteException e) {
				exceptionList.add(listener);
			}
		mListeners.removeAll(exceptionList);
	}

	@Override
	public PlayingBean getPlayingBean() throws RemoteException, PlayerException {
		return mPlayingBean;
	}

	@Override
	public void playPause() throws PlayerException {
		if (mPlayingBean != null) {
			mPlayingBean
					.setState((mPlayingBean.getState() == STATE.PLAY) ? STATE.PAUSE
							: STATE.PLAY);
			informPlayingBean(mPlayingBean);
		}
	}

	protected String getStreamUrl(String script, String url)
			throws PlayerException {
		try {
			String[] processArgs = new String[] { script, "-g", url };
			Process process = Runtime.getRuntime().exec(processArgs);
			InputStreamReader input = new InputStreamReader(
					process.getErrorStream());
			BufferedReader reader = new BufferedReader(input);
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.contains("Error"))
					throw new PlayerException("Error get stream :" + line);
			}
			input = new InputStreamReader(process.getInputStream());
			reader = new BufferedReader(input);
			String streamUrl = reader.readLine();
			if (streamUrl == null)
				throw new PlayerException("Get invalid stream url: null");
			return streamUrl;
		} catch (IOException e) {
			throw new PlayerException("Error get stream :" + e.getMessage());
		}
	}

	@Override
	public void play(String file) {
		if (mPlayingBean != null) {
			mPlayingBean.setState(STATE.PLAY);
			mPlayingBean.setFile(file);
			mPlayingBean.setStartTime(System.currentTimeMillis());
			informPlayingBean(mPlayingBean);
		}
	}

	@Override
	public void quit() throws PlayerException {
		if (mPlayingBean == null)
			mPlayingBean = new PlayingBean();
		mPlayingBean.setState(STATE.DOWN);
		mPlayingBean.setArtist(null);
		mPlayingBean.setTitle(null);
		mPlayingBean.setAlbum(null);
		mPlayingBean.setFile(null);
		mPlayingBean.setPath(null);
		mPlayingBean.setRadio(null);
		informPlayingBean(mPlayingBean);
	}

	@Override
	public void next() throws PlayerException {
		if (mPlayingBean != null) {
			mPlayingBean.setState(STATE.PLAY);
			informPlayingBean(mPlayingBean);
		}
	}

	@Override
	public void previous() throws PlayerException {
		if (mPlayingBean != null) {
			mPlayingBean.setState(STATE.PLAY);
			informPlayingBean(mPlayingBean);
		}
	}

	protected void loadThumbnail(PlayingBean bean) {
		if (bean.getArtist() != null && bean.getArtist().length() > 0) {
			try {
				bean.setThumbnailRGB(null);
				bean.setThumbnailSize(0, 0);
				PlayerThumbnailJob job = new PlayerThumbnailJob(bean, this);
				ThumbnailHandler.instance().queueThumbnailJob(job);
			} catch (Exception e) {
				RemoteLogger.performLog(LogPriority.ERROR, "No thumbnail for "
						+ bean.getArtist() + " ("
						+ e.getClass().getSimpleName() + ")", "Mediaserver");
			}
		}
	}

	public static BufferedImage searchImageFromGoogle(String search,
			int minResolution, int maxResolution) throws IOException,
			JSONException {
		URL url = new URL(
				"https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q="
						+ URLEncoder.encode(search, "UTF-8"));
		URLConnection connection = url.openConnection();

		String line;
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}

		JSONObject json = new JSONObject(builder.toString());
		JSONArray imageArray = json.getJSONObject("responseData").getJSONArray(
				"results");
		for (int i = 0; i < imageArray.length(); i++) {
			int width = imageArray.getJSONObject(i).getInt("width");
			int height = imageArray.getJSONObject(i).getInt("height");
			if (width * height >= minResolution
					&& width * height <= maxResolution) {
				String imageUrl = imageArray.getJSONObject(i).getString("url");
				return ImageIO.read(new URL(imageUrl));
			}
		}
		throw new IOException("No matching resolution found");
	}

	@Override
	public void playFromYoutube(String url) throws RemoteException,
			PlayerException {
	}

	@Override
	public void playFromArdMediathek(String url) throws RemoteException,
			PlayerException {

	}

	@Override
	public void onThumbnailCalculation(ThumbnailJob job) {
		if (job instanceof PlayerThumbnailJob) {
			PlayerThumbnailJob playerJob = (PlayerThumbnailJob) job;
			if (playerJob.player == this && playerJob.bean != null
					&& playerJob.thumbnail != null) {
				playerJob.bean.setThumbnailRGB(playerJob.thumbnail.rgb);
				playerJob.bean.setThumbnailSize(playerJob.thumbnail.width,
						playerJob.thumbnail.height);
				informPlayingBean(playerJob.bean);
			}
		}
	}

	class PlayingTimeCounter extends Thread {

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				if (mPlayingBean != null && mPlayingBean.getState() == STATE.PLAY) {
					mPlayingBean.incrementCurrentTime(1);
				}
			}
		}

	}

	class PlayerThumbnailJob extends ThumbnailJob {

		private PlayingBean bean;
		private AbstractPlayer player;

		public PlayerThumbnailJob(PlayingBean bean, AbstractPlayer player) {
			this.bean = bean;
			this.player = player;
		}

		@Override
		protected void calculateThumbnail() {
			thumbnail = ThumbnailHandler.instance().searchStringThumbnail(
					bean.getArtist());
			if (thumbnail == null) {
				try {
					BufferedImage image = searchImageFromGoogle(
							bean.getArtist(), 0, 1000 * 1000);
					thumbnail = new Thumbnail();
					File thumbnailFile = ThumbnailHandler.instance()
							.getStringThumbnailFile(bean.getArtist());
					BufferedImage thumbnailImage = ThumbnailHandler.instance()
							.toBufferedImage(
									ThumbnailHandler.instance()
											.createThumbnail(image,
													THUMBNAIL_SIZE));
					int rgb[] = ThumbnailHandler.instance().readImageIntArray(
							thumbnailImage);
					rgb = ThumbnailHandler.compressRGB565(rgb, THUMBNAIL_SIZE,
							THUMBNAIL_SIZE);
					thumbnail.width = THUMBNAIL_SIZE;
					thumbnail.height = THUMBNAIL_SIZE;
					thumbnail.rgb = rgb;
					ThumbnailHandler.instance().writeThumbnail(thumbnailFile,
							thumbnail);
				} catch (IOException | JSONException e) {
					RemoteLogger
							.performLog(
									LogPriority.ERROR,
									"Could not build thumbnail for '"
											+ bean.getArtist() + "': "
											+ e.getMessage(), "Mediaserver");
				}
			}
		}

		@Override
		protected Thumbnail readThumbnail() {
			thumbnail = ThumbnailHandler.instance().searchStringThumbnail(
					bean.getArtist());
			bean.setThumbnailSize(thumbnail.width, thumbnail.height);
			bean.setThumbnailRGB(thumbnail.rgb);
			return thumbnail;
		}

		@Override
		protected boolean needsCalculation() {
			thumbnail = ThumbnailHandler.instance().searchStringThumbnail(
					bean.getArtist());
			return thumbnail == null;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof PlayerThumbnailJob) {
				PlayerThumbnailJob pJob = (PlayerThumbnailJob) obj;
				return pJob.bean.getArtist().equals(bean.getArtist());
			}
			return super.equals(obj);
		}

	}

	public static void main(String[] args) {
		try {
			BufferedImage image = searchImageFromGoogle("hin", 100, 1000 * 600);
			System.out.println("w=" + image.getWidth() + ", h="
					+ image.getHeight());
		} catch (IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
