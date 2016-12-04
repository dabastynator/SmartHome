package de.neo.remote.mobile.persistence;

import de.neo.android.persistence.DomainBase;
import de.neo.android.persistence.Persistent;

public class RemoteServer extends DomainBase {

	@Persistent
	private String mIP;

	@Persistent
	private String mName;

	@Persistent
	private boolean mIsFavorite;

	@Persistent
	private String mEndPoint;

	@Persistent
	private String mApiToken;

	public String getIP() {
		return mIP;
	}

	public void setIP(String mIP) {
		this.mIP = mIP;
	}

	public boolean isFavorite() {
		return mIsFavorite;
	}

	public void setFavorite(boolean mIsFavorite) {
		this.mIsFavorite = mIsFavorite;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	@Override
	public String toString() {
		return mName;
	}

	public String getEndPoint() {
		return mEndPoint;
	}

	public void setEndPoint(String endPoint) {
		mEndPoint = endPoint;
	}

	public String getApiToken() {
		return mApiToken;
	}

	public void setApiToken(String apiToken) {
		mApiToken = apiToken;
	}
	
	

}
