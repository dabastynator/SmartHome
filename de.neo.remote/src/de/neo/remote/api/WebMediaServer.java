package de.neo.remote.api;

import de.neo.rmi.api.WebField;

public class WebMediaServer {

	@WebField(name = "id")
	private String mID;

	@WebField(name = "current_playing")
	private PlayingBean mCurrentPlaying;

	public String getID() {
		return mID;
	}

	public void setID(String iD) {
		mID = iD;
	}

	public PlayingBean getCurrentPlaying() {
		return mCurrentPlaying;
	}

	public void setCurrentPlaying(PlayingBean currentPlaying) {
		mCurrentPlaying = currentPlaying;
	}

}
