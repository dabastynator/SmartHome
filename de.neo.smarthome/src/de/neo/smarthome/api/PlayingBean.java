package de.neo.smarthome.api;

import java.io.Serializable;

import de.neo.remote.web.WebField;

public class PlayingBean implements Serializable {

	public enum STATE {
		PLAY, PAUSE, DOWN
	};

	private static final long serialVersionUID = 991634045875693050L;

	@WebField(name = "artist")
	public String mArtist;

	@WebField(name = "title")
	public String mTitle;

	@WebField(name = "album")
	public String mAlbum;

	@WebField(name = "radio")
	public String mRadio;

	@WebField(name = "file")
	public String mFile;

	@WebField(name = "path")
	public String mPath;

	@WebField(name = "state")
	public STATE mState;

	@WebField(name = "durationSec")
	public int mDurationSec;
	
	@WebField(name = "inTrackSec")
	public int mInTrackSec;

	private int thumbnailWidth;
	private int thumbnailHeight;
	private int[] thumbnailRGB;

	@WebField(name = "volume")
	public int mVolume;

	public PlayingBean()
	{
	}

	public PlayingBean(PlayingBean bean) {
		if (bean == null)
			return;
		mAlbum = bean.mAlbum;
		mArtist = bean.mArtist;
		mTitle = bean.mTitle;
		mFile = bean.mFile;
		mRadio = bean.mRadio;
		mState = bean.mState;
		mPath = bean.mPath;
		mDurationSec = bean.mDurationSec;
		mInTrackSec = bean.mInTrackSec;
		thumbnailWidth = bean.getThumbnailWidth();
		thumbnailHeight = bean.getThumbnailHeight();
		thumbnailRGB = bean.getThumbnailRGB();
	}

	public PlayingBean(String artist, String album, String title, String file, String radio, String path, STATE state) {
		mArtist = artist;
		mAlbum = album;
		mTitle = title;
		mFile = file;
		mRadio = radio;
		mState = state;
		mPath = path;
	}


	public void parseICYInfo(String line) {
		mArtist = null;
		mFile = null;
		mRadio = null;
		mState = null;
		mPath = null;
		String title = line.substring(23);
		title = title.substring(0, title.indexOf('\''));
		String[] split = title.split("-");
		mArtist = split[0].trim();
		mTitle = split[split.length - 1].trim();
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
