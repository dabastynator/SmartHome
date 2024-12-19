package de.neo.smarthome.mediaserver;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.AbstractID3v2;
import org.json.JSONException;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.api.PlayerException;
import de.neo.smarthome.api.PlayingBean;
import de.neo.smarthome.api.PlayingBean.STATE;

/**
 * the abstract player implements basically functions of an player. this
 * contains handling of listeners, current playing file
 * 
 * @author sebastian
 */
public abstract class AbstractPlayer implements IPlayer {

	public static final String YOUTUBE_DL_FILE = "/usr/bin/youtube-dl";

	public static final int THUMBNAIL_SIZE = 128;

	/**
	 * current playing file
	 */
	protected PlayingBean mPlayingBean;

	protected int mVolume = 50;

	public AbstractPlayer() {
		// ThumbnailHandler.instance().calculationListener().add(this);
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
		bean.mVolume = mVolume;
		readFileTags(file, bean);
		return bean;
	}

	public static void readFileTags(File file, PlayingBean bean) {
		// Just check
		if (file.length() > 1000 * 1000 * 100)
			return;
		// First use jid3lib to read metadata
		// jid3lib may fail reading some data
		// so use mp3agic in second step
		try {
			MP3File mp3File = new MP3File(file);
			bean.mFile = file.getName().trim();
			bean.mPath = file.getPath();
			AbstractID3v2 id3v2Tag = mp3File.getID3v2Tag();
			if (id3v2Tag != null) {
				if (id3v2Tag.getAuthorComposer() != null)
					bean.mArtist = id3v2Tag.getAuthorComposer().trim();
				if (id3v2Tag.getSongTitle() != null)
					bean.mTitle = id3v2Tag.getSongTitle().trim();
				if (id3v2Tag.getAlbumTitle() != null)
					bean.mAlbum = id3v2Tag.getAlbumTitle().trim();
			}
		} catch (TagException | IOException e) {
			RemoteLogger.performLog(LogPriority.ERROR, "Cant read file-information: " + e.getMessage(), "Mediaserver");
		}
		try {
			Mp3File mp3File = new Mp3File(file.getAbsolutePath());
			bean.mFile = file.getName().trim();
			bean.mPath = file.getPath();
			if (mp3File.hasId3v1Tag()) {
				ID3v1 tag = mp3File.getId3v1Tag();
				if (tag.getArtist() != null)
					bean.mArtist = tag.getArtist().trim();
				if (tag.getTitle() != null)
					bean.mTitle = tag.getTitle().trim();
				if (tag.getAlbum() != null)
					bean.mAlbum = tag.getAlbum().trim();
			}
			if (mp3File.hasId3v2Tag()) {
				ID3v2 tag = mp3File.getId3v2Tag();
				if (tag.getArtist() != null)
					bean.mArtist = tag.getArtist().trim();
				if (tag.getTitle() != null)
					bean.mTitle = tag.getTitle().trim();
				if (tag.getAlbum() != null)
					bean.mAlbum = tag.getAlbum().trim();
			}
		} catch (UnsupportedTagException | InvalidDataException | IOException e) {
			RemoteLogger.performLog(LogPriority.ERROR, "Cant read file-information: " + e.getMessage(), "Mediaserver");
		}
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
		bean.mState = STATE.PLAY;
		informPlayingBean(bean);
	}

	/**
	 * A new bean will be created.
	 * 
	 * @param bean
	 */
	protected void informPlayingBean(PlayingBean bean) {
		mPlayingBean = new PlayingBean(bean);
		mPlayingBean.mVolume = mVolume;
	}

	@Override
	public PlayingBean getPlayingBean() throws RemoteException {
		return mPlayingBean;
	}

	@Override
	public void playPause() throws PlayerException {
		if (mPlayingBean != null) {
			mPlayingBean.mState = (mPlayingBean.mState == STATE.PLAY) ? STATE.PAUSE : STATE.PLAY;
			informPlayingBean(mPlayingBean);
		}
	}

	protected String getStreamUrl(String script, String url) throws PlayerException {
		try {
			String[] processArgs = new String[] { script, "-g", url };
			Process process = Runtime.getRuntime().exec(processArgs);
			InputStreamReader input = new InputStreamReader(process.getErrorStream());
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
			mPlayingBean.mState = STATE.PLAY;
			mPlayingBean.mFile = file;
			informPlayingBean(mPlayingBean);
		}
	}

	@Override
	public void quit() throws PlayerException {
		if (mPlayingBean == null)
			mPlayingBean = new PlayingBean();
		mPlayingBean.mState = STATE.DOWN;
		mPlayingBean.mArtist = null;
		mPlayingBean.mTitle = null;
		mPlayingBean.mAlbum = null;
		mPlayingBean.mFile = null;
		mPlayingBean.mPath = null;
		mPlayingBean.mRadio = null;
		informPlayingBean(mPlayingBean);
	}

	@Override
	public void next() throws PlayerException {
		if (mPlayingBean != null) {
			mPlayingBean.mState = STATE.PLAY;
			informPlayingBean(mPlayingBean);
		}
	}

	@Override
	public void previous() throws PlayerException {
		if (mPlayingBean != null) {
			mPlayingBean.mState = STATE.PLAY;
			informPlayingBean(mPlayingBean);
		}
	}

	protected void loadThumbnail(PlayingBean bean) {
		// if (bean.getArtist() != null && bean.getArtist().length() > 0) {
		// try {
		// bean.setThumbnailRGB(null);
		// bean.setThumbnailSize(0, 0);
		// PlayerThumbnailJob job = new PlayerThumbnailJob(bean, this);
		// ThumbnailHandler.instance().queueThumbnailJob(job);
		// } catch (Exception e) {
		// RemoteLogger.performLog(LogPriority.ERROR,
		// "No thumbnail for " + bean.getArtist() + " (" +
		// e.getClass().getSimpleName() + ")",
		// "Mediaserver");
		// }
		// }
	}

	public static BufferedImage searchImageFromGoogle(String search, int minResolution, int maxResolution)
			throws IOException, JSONException {
		throw new IOException("not supported");
		/*
		 * URL url = new URL(
		 * "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=" +
		 * URLEncoder.encode(search, "UTF-8")); URLConnection connection =
		 * url.openConnection();
		 * 
		 * String line; StringBuilder builder = new StringBuilder();
		 * BufferedReader reader = new BufferedReader(new
		 * InputStreamReader(connection.getInputStream())); while ((line =
		 * reader.readLine()) != null) { builder.append(line); }
		 * 
		 * JSONObject json = new JSONObject(builder.toString()); JSONArray
		 * imageArray =
		 * json.getJSONObject("responseData").getJSONArray("results"); for (int
		 * i = 0; i < imageArray.length(); i++) { int width =
		 * imageArray.getJSONObject(i).getInt("width"); int height =
		 * imageArray.getJSONObject(i).getInt("height"); if (width * height >=
		 * minResolution && width * height <= maxResolution) { String imageUrl =
		 * imageArray.getJSONObject(i).getString("url"); return ImageIO.read(new
		 * URL(imageUrl)); } } throw new IOException(
		 * "No matching resolution found");
		 */
	}

	@Override
	public void playFromYoutube(String url) throws RemoteException, PlayerException {
	}

	// @Override
	// public void onThumbnailCalculation(ThumbnailJob job) {
	// if (job instanceof PlayerThumbnailJob) {
	// PlayerThumbnailJob playerJob = (PlayerThumbnailJob) job;
	// if (playerJob.player == this && playerJob.bean != null &&
	// playerJob.thumbnail != null) {
	// playerJob.bean.setThumbnailRGB(playerJob.thumbnail.rgb);
	// playerJob.bean.setThumbnailSize(playerJob.thumbnail.width,
	// playerJob.thumbnail.height);
	// informPlayingBean(playerJob.bean);
	// }
	// }
	// }


	// class PlayerThumbnailJob extends ThumbnailJob {
	//
	// private PlayingBean bean;
	// private AbstractPlayer player;
	//
	// public PlayerThumbnailJob(PlayingBean bean, AbstractPlayer player) {
	// this.bean = bean;
	// this.player = player;
	// }
	//
	// @Override
	// protected void calculateThumbnail() {
	// thumbnail =
	// ThumbnailHandler.instance().searchStringThumbnail(bean.getArtist());
	// if (thumbnail == null) {
	// try {
	// BufferedImage image = searchImageFromGoogle(bean.getArtist(), 0, 1000 *
	// 1000);
	// thumbnail = new Thumbnail();
	// File thumbnailFile =
	// ThumbnailHandler.instance().getStringThumbnailFile(bean.getArtist());
	// BufferedImage thumbnailImage = ThumbnailHandler
	// .toBufferedImage(ThumbnailHandler.instance().createThumbnail(image,
	// THUMBNAIL_SIZE));
	// int rgb[] =
	// ThumbnailHandler.instance().readImageIntArray(thumbnailImage);
	// rgb = ThumbnailHandler.compressRGB565(rgb, THUMBNAIL_SIZE,
	// THUMBNAIL_SIZE);
	// thumbnail.width = THUMBNAIL_SIZE;
	// thumbnail.height = THUMBNAIL_SIZE;
	// thumbnail.rgb = rgb;
	// ThumbnailHandler.instance().writeThumbnail(thumbnailFile, thumbnail);
	// } catch (IOException | JSONException e) {
	// RemoteLogger.performLog(LogPriority.ERROR,
	// "Could not build thumbnail for '" + bean.getArtist() + "': " +
	// e.getMessage(),
	// "Mediaserver");
	// }
	// }
	// }
	//
	// @Override
	// protected Thumbnail readThumbnail() {
	// thumbnail =
	// ThumbnailHandler.instance().searchStringThumbnail(bean.getArtist());
	// bean.setThumbnailSize(thumbnail.width, thumbnail.height);
	// bean.setThumbnailRGB(thumbnail.rgb);
	// return thumbnail;
	// }
	//
	// @Override
	// protected boolean needsCalculation() {
	// thumbnail =
	// ThumbnailHandler.instance().searchStringThumbnail(bean.getArtist());
	// return thumbnail == null;
	// }
	//
	// @Override
	// public boolean equals(Object obj) {
	// if (obj instanceof PlayerThumbnailJob) {
	// PlayerThumbnailJob pJob = (PlayerThumbnailJob) obj;
	// return pJob.bean.getArtist().equals(bean.getArtist());
	// }
	// return super.equals(obj);
	// }
	//
	// }

	public static void main(String[] args) {
		PlayingBean bean = new PlayingBean();
		readFileTags(new File("/home/sebastian/Musik/01 - Kryptic Minds - Intro.mp3"), bean);
		System.out.println(bean.mArtist + " - " + bean.mAlbum + " - " + bean.mTitle);
		bean = new PlayingBean();
		readFileTags(new File("/home/sebastian/Musik/Pichl Michl - Track 2.mp3"), bean);
		System.out.println(bean.mArtist + " - " + bean.mAlbum + " - " + bean.mTitle);
	}

	@Override
	public void setVolume(int volume) throws RemoteException, PlayerException {
		if (volume > mVolume)
			volDown();
		if (volume < mVolume)
			volUp();
		mVolume = volume;
	}

	@Override
	public int getVolume() throws RemoteException, PlayerException {
		return mVolume;
	}
}
