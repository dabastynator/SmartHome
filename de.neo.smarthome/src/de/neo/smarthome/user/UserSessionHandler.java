package de.neo.smarthome.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.user.User.UserRole;

public class UserSessionHandler {

	// Default duration are 10 days
	public static final long DEFALT_DURATION = 1000 * 3600 * 24 * 10;

	private Map<String, UserSession> mSessions = new HashMap<>();
	private Map<User, UserSession> mUserSessions = new HashMap<>();

	private Random mRandom;

	private static UserSessionHandler mInstance;

	public static UserSessionHandler getSingleton() {
		if (mInstance == null) {
			mInstance = new UserSessionHandler();
		}
		return mInstance;
	}

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

	public static User require(String token) throws RemoteException {
		UserSession session = getSingleton().find(token);
		if (session == null) {
			throw new RemoteException("Invalid user token. Requre a new one");
		}
		return session.mUser;
	}

	public static User require(String token, UserRole role) throws RemoteException {
		User user = require(token);
		if (user.getRole() != UserRole.ADMIN) {
			throw new RemoteException("Access denied. Invalid admin token");
		}
		return user;
	}

	public UserSession generate(User user, Long expiration) {
		UserSession result = mUserSessions.get(user);
		if (result != null) {
			return result;
		}
		result = new UserSession();
		result.mExpiration = expiration;
		result.mToken = "";
		for (int i = 0; i < 4; i++) {
			result.mToken += Integer.toHexString(mRandom.nextInt());
		}
		result.mUser = user;
		mSessions.put(result.mToken, result);
		mUserSessions.put(user, result);
		RemoteLogger.performLog(LogPriority.INFORMATION, "Generate token for " + user.getName(), "SessionHandler");
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
