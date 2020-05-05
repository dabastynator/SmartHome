package de.neo.smarthome.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class UserSessionHandler {

	// Default duration are 10 days
	public static final long DEFALT_DURATION = 1000 * 3600 * 24 * 10;

	private Map<String, UserSession> mSessions = new HashMap<>();
	private Map<User, UserSession> mUserSessions = new HashMap<>();

	private Random mRandom;

	public UserSessionHandler() {
		mRandom = new Random();
		mRandom.setSeed(System.currentTimeMillis());
	}

	public UserSession find(String token) {
		UserSession session = mSessions.get(token);
		if (session != null) {
			if (session.mExpiration != 0 && session.mExpiration < System.currentTimeMillis()) {
				mSessions.remove(token);
				session = null;
			}
		}
		return session;
	}

	public UserSession generate(User user, Long expiration) {
		UserSession result = mUserSessions.get(user);
		if (result != null) {
			return result;
		}
		result = new UserSession();
		result.mExpiration = expiration;
		for (int i = 0; i < 8; i++) {
			result.mToken += Integer.toHexString(mRandom.nextInt());
		}
		result.mUser = user;
		mSessions.put(result.mToken, result);
		mUserSessions.put(user, result);
		return result;
	}

	public static class UserSession {

		private User mUser;

		private Long mExpiration;

		private String mToken;

		public User getUser() {
			return mUser;
		}

		public void setUser(User user) {
			mUser = user;
		}

		public Long getExpiration() {
			return mExpiration;
		}

		public void setExpiration(Long expiration) {
			mExpiration = expiration;
		}

		public String getToken() {
			return mToken;
		}

		public void setToken(String token) {
			mToken = token;
		}

	}
}
