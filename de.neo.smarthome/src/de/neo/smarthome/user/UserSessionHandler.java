package de.neo.smarthome.user;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.neo.persist.Dao;
import de.neo.persist.DaoException;
import de.neo.persist.DaoFactory;
import de.neo.persist.annotations.Domain;
import de.neo.persist.annotations.Id;
import de.neo.persist.annotations.OnLoad;
import de.neo.persist.annotations.Persist;
import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.user.User.UserRole;

public class UserSessionHandler {

	public enum SessionType {
		VOLATILE, PERSISTENT
	}

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
		Dao<UserSession> dao = DaoFactory.getInstance().getDao(UserSession.class);
		try {
			for (UserSession session : dao.loadAll()) {
				mSessions.put(session.mToken, session);
				mUserSessions.put(session.mUser, session);
			}
		} catch (DaoException e) {
			RemoteLogger.performLog(LogPriority.ERROR,
					"Error loading sessions: " + e.getMessage() + " (" + e.getClass().getSimpleName() + ")",
					"SessionHandler");
		}
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

	public void delete(String deleteToken) {
		mSessions.remove(deleteToken);
	}

	public static User require(String token) throws RemoteException {
		UserSession session = getSingleton().find(token);
		if (session == null) {
			throw new RemoteException("Invalid user token. Require a new one");
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

	public Collection<UserSession> list() {
		return mSessions.values();
	}

	public UserSession generate(User user, Long expiration, SessionType type) {
		UserSession result = mUserSessions.get(user);
		if (result != null && type == SessionType.VOLATILE) {
			return result;
		}
		result = new UserSession();
		result.mExpiration = expiration;
		result.mToken = "";
		result.mType = type;
		for (int i = 0; i < 2; i++) {
			result.mToken += Integer.toHexString(mRandom.nextInt());
		}
		result.mUser = user;

		mSessions.put(result.mToken, result);
		mUserSessions.put(user, result);
		RemoteLogger.performLog(LogPriority.INFORMATION, "Generate token for " + user.getName(), "SessionHandler");
		return result;
	}

	@Domain(name = "Session")
	public static class UserSession {

		@Id(name = "id")
		private long mId;

		@Persist(name = "user")
		private User mUser;

		@Persist(name = "token")
		private String mToken;

		private long mExpiration;

		private SessionType mType = SessionType.VOLATILE;

		public User getUser() {
			return mUser;
		}

		public void setUser(User user) {
			mUser = user;
		}

		public long getExpiration() {
			return mExpiration;
		}

		public void setExpiration(long expiration) {
			mExpiration = expiration;
		}

		public String getToken() {
			return mToken;
		}

		public void setToken(String token) {
			mToken = token;
		}

		public long getId() {
			return mId;
		}

		public void setId(long id) {
			mId = id;
		}

		public SessionType getType() {
			return mType;
		}

		public void setType(SessionType type) {
			mType = type;
		}

		@OnLoad
		private void onLoaded() {
			mType = SessionType.PERSISTENT;
		}
	}

}
