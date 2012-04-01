package de.remote.api;

import java.io.Serializable;

/**
 * contains playing information
 * @author sebastian
 */
public class PlayingBean implements Serializable{

	/**
	 * states of the player
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
	 * current state
	 */
	private STATE state;


	public PlayingBean(PlayingBean bean) {
		if (bean == null)
			return;
		album = bean.getAlbum();
		artist = bean.getArtist();
		title = bean.getTitle();
		file = bean.getFile();
		radio = bean.getRadio();
		state = bean.getState();
	}


	public PlayingBean() {
	}


	public String getArtist() {
		return artist;
	}


	public void setArtist(String artist) {
		this.artist = artist;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public String getAlbum() {
		return album;
	}


	public void setAlbum(String album) {
		this.album = album;
	}


	public String getRadio() {
		return radio;
	}


	public void setRadio(String radio) {
		this.radio = radio;
	}


	public STATE getState() {
		return state;
	}


	public void setState(STATE state) {
		this.state = state;
	}


	public String getFile() {
		return file;
	}


	public void setFile(String file) {
		this.file = file;
	}
	
	
}
