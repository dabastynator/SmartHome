package de.neo.smarthome.mobile.persistence;

import de.neo.android.persistence.DomainBase;
import de.neo.android.persistence.Persistent;

public class RemoteServer extends DomainBase {

	@Persistent
	private String mName;

	@Persistent
	private boolean mIsFavorite;

	@Persistent
	private String mEndPoint;

	@Persistent
	private String mApiToken;

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
