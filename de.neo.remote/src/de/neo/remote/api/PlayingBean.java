package de.neo.remote.api;

import java.io.Serializable;

/**
 * contains playing information
 * 
 * @author sebastian
 */
public class PlayingBean implements Serializable {

	/**
	 * states of the player
	 * 
	 * @author sebastian
	 */
	public enum STATE {
		PLAY, PAUSE, DOWN
	};

	/**
	 * generated uid
	 */
	private static final long serialVersionUID = 991634045875693050L;

	/**
	 * current artist
	 */
	private String artist;

	/**
	 * current title;
	 */
	private String title;

	/**
	 * current album
	 */
	private String album;

	/**
	 * current radio
	 */
	private String radio;

	/**
	 * current file
	 */
	private String file;

	/**
	 * current path of playing file
	 */
	private String path;

	/**
	 * current state
	 */
	private STATE state;

	/**
	 * current playing time in seconds
	 */
	private long startTime;

	/**
	 * length of file in seconds
	 */
	private int lengthTime;
	
	/**
	 * x and y size of the thumbnail 
	 */
	private int thumbnailWidth;
	private int thumbnailHeight;
	
	/**
	 * rgb content of the thumbnail in RGB_565
	 */
	private int[] thumbnailRGB;

	/**
	 * allocate new bean
	 */
	public PlayingBean() {
		startTime = System.currentTimeMillis();
	}

	/**
	 * allocate new bean and copy all attributes
	 * 
	 * @param bean
	 */
	public PlayingBean(PlayingBean bean) {
		if (bean == null)
			return;
		album = bean.getAlbum();
		artist = bean.getArtist();
		title = bean.getTitle();
		file = bean.getFile();
		radio = bean.getRadio();
		state = bean.getState();
		path = bean.getPath();
		startTime = bean.getStartTime();
		lengthTime = bean.getLengthTime();
		thumbnailWidth = bean.getThumbnailWidth();
		thumbnailHeight = bean.getThumbnailHeight();
		thumbnailRGB = bean.getThumbnailRGB();
	}

	/**
	 * allocation new bean and set values
	 * 
	 * @param artist
	 * @param album
	 * @param title
	 * @param file
	 * @param radio
	 * @param state
	 */
	public PlayingBean(String artist, String album, String title, String file,
			String radio, String path, STATE state) {
		this.artist = artist;
		this.album = album;
		this.title = title;
		this.file = file;
		this.radio = radio;
		this.state = state;
		this.path = path;
	}

	/**
	 * @return artist
	 */
	public String getArtist() {
		return artist;
	}

	/**
	 * @param artist
	 */
	public void setArtist(String artist) {
		this.artist = artist;
	}

	/**
	 * @return title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return album
	 */
	public String getAlbum() {
		return album;
	}

	/**
	 * @param album
	 */
	public void setAlbum(String album) {
		this.album = album;
	}

	/**
	 * @return radio
	 */
	public String getRadio() {
		return radio;
	}

	/**
	 * @param radio
	 */
	public void setRadio(String radio) {
		this.radio = radio;
	}

	/**
	 * @return state
	 */
	public STATE getState() {
		return state;
	}

	/**
	 * @param state
	 */
	public void setState(STATE state) {
		this.state = state;
	}

	/**
	 * @return file
	 */
	public String getFile() {
		return file;
	}

	/**
	 * @param file
	 */
	public void setFile(String file) {
		this.file = file;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long l) {
		this.startTime = l;
		if (l < 2) {
			l = 2;
		}
	}

	public int getLengthTime() {
		return lengthTime;
	}

	public void setLengthTime(int lengthTime) {
		this.lengthTime = lengthTime;
	}

	public void parseICYInfo(String line) {
		this.artist = null;
		this.album = null;
		this.title = null;
		this.file = null;
		this.radio = null;
		this.state = null;
		this.path = null;
		this.lengthTime = -1;
		this.startTime = System.currentTimeMillis();
		String title = line.substring(23);
		title = title.substring(0, title.indexOf('\''));
		String[] split = title.split("-");
		this.artist = split[0].trim();
		this.title = split[split.length - 1].trim();
	}

	public void incrementCurrentTime(int seekValue) {
		startTime += seekValue;
	}

	public int getThumbnailWidth() {
		return thumbnailWidth;
	}
	
	public int getThumbnailHeight() {
		return thumbnailHeight;
	}

	public void setThumbnailSize(int width, int height) {
		thumbnailWidth = width;
		thumbnailHeight = height;
	}

	public int[] getThumbnailRGB() {
		return thumbnailRGB;
	}

	public void setThumbnailRGB(int[] thumbnailRGB) {
		this.thumbnailRGB = thumbnailRGB;
	}

}
