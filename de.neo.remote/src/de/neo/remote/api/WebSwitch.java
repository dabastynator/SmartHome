package de.neo.remote.api;

import de.neo.rmi.api.WebField;

public class WebSwitch {

	@WebField(name = "id")
	private String mID;

	@WebField(name = "name")
	private String mName;

	@WebField(name = "state")
	private String mState;

	public String getID() {
		return mID;
	}

	public void setID(String iD) {
		mID = iD;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getState() {
		return mState;
	}

	public void setState(String state) {
		mState = state;
	}

}
