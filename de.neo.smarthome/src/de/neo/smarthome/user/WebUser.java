package de.neo.smarthome.user;

import java.util.ArrayList;
import java.util.List;

import de.neo.persist.Dao;
import de.neo.persist.DaoException;
import de.neo.persist.DaoFactory;
import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebGet;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.AbstractUnitHandler;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.SmartHome.ControlUnitFactory;
import de.neo.smarthome.api.IControlCenter.BeanWeb;
import de.neo.smarthome.api.IControllUnit;
import de.neo.smarthome.api.IWebUser;
import de.neo.smarthome.controlcenter.ControlCenter;
import de.neo.smarthome.user.UnitAccessHandler.UserAccessList;
import de.neo.smarthome.user.User.UserRole;
import de.neo.smarthome.user.UserSessionHandler.SessionType;
import de.neo.smarthome.user.UserSessionHandler.UserSession;

public class WebUser extends AbstractUnitHandler implements IWebUser {

	public WebUser(ControlCenter center) {
		super(center);
	}

	private BeanUser toBean(User user) {
		BeanUser bean = new BeanUser();
		bean.setName(user.getName());
		bean.setId(user.getId());
		bean.setRole(user.getRole());
		return bean;
	}

	private BeanUserToken toBean(UserSession session) {
		BeanUserToken token = new BeanUserToken();
		token.setExpiration(session.getExpiration());
		token.setToken(session.getToken());
		token.setType(session.getType());
		token.setUser(session.getUser().getName());
		return token;
	}

	private User userById(long userId) throws DaoException, RemoteException {
		Dao<User> userDao = DaoFactory.getInstance().getDao(User.class);
		User user = userDao.loadById(userId);
		if (user != null) {
			return user;
		}
		throw new RemoteException("Unknown user-id: " + userId);
	}
	
	@WebRequest(path = "current", description = "Get user of specified token")
	public BeanUser currentUser(@WebGet(name = "token") String token) throws RemoteException, DaoException{
		User user = UserSessionHandler.require(token);
		return toBean(user);
	}

	@WebRequest(path = "list", description = "List all users of the controlcenter", genericClass = BeanUser.class)
	public ArrayList<BeanUser> getUsers(@WebGet(name = "token") String adminToken)
			throws RemoteException, DaoException {
		UserSessionHandler.require(adminToken, UserRole.ADMIN);
		Dao<User> userDao = DaoFactory.getInstance().getDao(User.class);
		ArrayList<BeanUser> result = new ArrayList<>();
		for (User user : userDao.loadAll()) {
			result.add(toBean(user));
		}
		return result;
	}

	@WebRequest(path = "create", description = "Creat new user")
	public BeanUser createUser(@WebGet(name = "token") String adminToken, @WebGet(name = "user_name") String userName,
			@WebGet(name = "password") String password, @WebGet(name = "avatar") String avatar)
			throws RemoteException, DaoException {
		UserSessionHandler.require(adminToken, UserRole.ADMIN);
		User user = new User();
		user.setName(userName);
		user.setPassword(password);
		user.setAvatar(avatar);
		Dao<User> userDao = DaoFactory.getInstance().getDao(User.class);
		userDao.save(user);
		RemoteLogger.performLog(LogPriority.INFORMATION, "Create new user " + user.getName(), "UserHandler");
		return toBean(user);
	}

	@WebRequest(path = "delete", description = "Delete user")
	public void deleteUsers(@WebGet(name = "token") String adminToken, @WebGet(name = "user_id") long userId)
			throws RemoteException, DaoException {
		UserSessionHandler.require(adminToken, UserRole.ADMIN);
		User user = userById(userId);
		Dao<User> userDao = DaoFactory.getInstance().getDao(User.class);
		RemoteLogger.performLog(LogPriority.INFORMATION, "Delete user " + user.getName(), "UserHandler");
		userDao.delete(user);
	}

	@Override
	@WebRequest(description = "Generate a user token", path = "generate_token")
	public BeanUserToken generateUserToken(@WebGet(name = "user_name") String userName,
			@WebGet(name = "password") String password) throws RemoteException, DaoException {
		Dao<User> userDao = DaoFactory.getInstance().getDao(User.class);
		List<User> userList = userDao.loadAll();
		if (userList.size() == 0 && User.DefaultRoot.matches(userName, password)) {
			userList.add(User.DefaultRoot);
		}
		for (User user : userList) {
			if (user.matches(userName, password)) {
				Long expiration = System.currentTimeMillis() + UserSessionHandler.DEFALT_DURATION;
				UserSession session = UserSessionHandler.getSingleton().generate(user, expiration,
						SessionType.VOLATILE);
				return toBean(session);
			}
		}
		throw new RemoteException("Invalid username or password");
	}

	private User changeableUser(String token, long userId) throws DaoException, RemoteException {
		if (userId > 0) {
			User currentUser = UserSessionHandler.require(token);
			if(currentUser.getId() == userId)
				return currentUser;
			UserSessionHandler.require(token, UserRole.ADMIN);
			return userById(userId);
		} else {
			return UserSessionHandler.require(token);
		}
	}

	@WebRequest(description = "Change password", path = "change_password")
	public void changePassword(@WebGet(name = "token") String token, @WebGet(name = "user_id") long userId,
			@WebGet(name = "new_password") String new_password) throws RemoteException, DaoException {
		User user = changeableUser(token, userId);
		user.setPassword(new_password);
		Dao<User> userDao = DaoFactory.getInstance().getDao(User.class);
		userDao.update(user);
		RemoteLogger.performLog(LogPriority.INFORMATION, "Change password for user " + user.getName(), "UserHandler");
	}

	@WebRequest(description = "Change avatar, as base64 encoded png", path = "change_avatar")
	public void changeAvatar(@WebGet(name = "token") String token,
			@WebGet(name = "user_id", required = false, defaultvalue = "0") long userId,
			@WebGet(name = "new_avatar") String newAvatar) throws RemoteException, DaoException {
		User user = changeableUser(token, userId);
		user.setAvatar(newAvatar);
		Dao<User> userDao = DaoFactory.getInstance().getDao(User.class);
		userDao.update(user);
		RemoteLogger.performLog(LogPriority.INFORMATION, "Change avatar for user " + user.getName(), "UserHandler");
	}
	
	@WebRequest(description = "Change user name", path = "change_name")
	public void changeName(@WebGet(name = "token") String token, @WebGet(name = "user_id", required = false, defaultvalue = "0") long userId,
			@WebGet(name = "new_name") String newName) throws RemoteException, DaoException{
		User user = changeableUser(token, userId);
		user.setName(newName);
		Dao<User> userDao = DaoFactory.getInstance().getDao(User.class);
		userDao.update(user);
		RemoteLogger.performLog(LogPriority.INFORMATION, "Change name for user " + user.getName(), "UserHandler");
	}

	@WebRequest(path = "add_access", description = "Add unit access for user")
	public void addUnitAccess(@WebGet(name = "token") String adminToken, @WebGet(name = "user_id") long userId,
			@WebGet(name = "unit_id") String unitId) throws RemoteException, DaoException {
		UserSessionHandler.require(adminToken, UserRole.ADMIN);
		User user = userById(userId);
		Dao<UnitAccess> accessDao = DaoFactory.getInstance().getDao(UnitAccess.class);
		UserAccessList accessList = mCenter.getAccessHandler().getAccessListByUser(user);
		IControllUnit unit = mCenter.getControlUnit(unitId);
		if (unit == null) {
			throw new RemoteException("Unknown unit: " + unitId);
		}
		if (accessList.getUnit(unitId) == null) {
			RemoteLogger.performLog(LogPriority.INFORMATION, "Add access for " + user.getName(), "UserHandler");
			UnitAccess access = new UnitAccess();
			access.setUser(user);
			access.setUnit(unit);
			accessDao.save(access);
			accessList.addAccess(access);
		}
	}

	@WebRequest(path = "get_access", description = "Get list of accessible units for user", genericClass = BeanWeb.class)
	public ArrayList<BeanWeb> getUnitAccess(@WebGet(name = "token") String adminToken,
			@WebGet(name = "user_id") long userId) throws RemoteException, DaoException {
		UserSessionHandler.require(adminToken, UserRole.ADMIN);
		User user = userById(userId);
		UserAccessList accessList = mCenter.getAccessHandler().getAccessListByUser(user);
		ArrayList<BeanWeb> result = new ArrayList<>();
		for (UnitAccess access : accessList.listAccess()) {
			result.add(access.getUnit().getWebBean());
		}
		return result;
	}

	@WebRequest(path = "remove_access", description = "Remove unit access for user")
	public void removeUnitAccess(@WebGet(name = "token") String adminToken, @WebGet(name = "user_id") long userId,
			@WebGet(name = "unit_id") String unitId) throws RemoteException, DaoException {
		UserSessionHandler.require(adminToken, UserRole.ADMIN);
		User user = userById(userId);
		Dao<UnitAccess> accessDao = DaoFactory.getInstance().getDao(UnitAccess.class);
		UserAccessList accessList = mCenter.getAccessHandler().getAccessListByUser(user);
		IControllUnit unit = mCenter.getControlUnit(unitId);
		if (unit == null) {
			throw new RemoteException("Unknown unit: " + unitId);
		}
		if (accessList.getUnit(unitId) != null) {
			RemoteLogger.performLog(LogPriority.INFORMATION, "Remove access for " + user.getName(), "UserHandler");
			UnitAccess access = new UnitAccess();
			access.setUser(user);
			access.setUnit(unit);
			accessDao.delete(access);
			accessList.addAccess(access);
		}
	}

	@Override
	@WebRequest(path = "list_tokens", description = "List all active tokens", genericClass = BeanUserToken.class)
	public ArrayList<BeanUserToken> listTokens(@WebGet(name = "token") String adminToken)
			throws RemoteException, DaoException {
		UserSessionHandler.require(adminToken, UserRole.ADMIN);
		ArrayList<BeanUserToken> list = new ArrayList<>();
		for (UserSession session : UserSessionHandler.getSingleton().list()) {
			list.add(toBean(session));
		}
		return list;
	}

	@Override
	@WebRequest(path = "create_persistent_token", description = "Create a persistent token for given user")
	public BeanUserToken createPersistentToken(@WebGet(name = "token") String adminToken,
			@WebGet(name = "user_id") long userId) throws RemoteException, DaoException {
		UserSessionHandler.require(adminToken, UserRole.ADMIN);
		User user = userById(userId);
		UserSession session = UserSessionHandler.getSingleton().generate(user, (long) 0, SessionType.PERSISTENT);
		Dao<UserSession> dao = DaoFactory.getInstance().getDao(UserSession.class);
		dao.save(session);
		return toBean(session);
	}

	@Override
	@WebRequest(path = "delete_token", description = "Delete token")
	public void deleteToken(@WebGet(name = "token") String adminToken,
			@WebGet(name = "delete_token") String deleteToken) throws RemoteException, DaoException {
		UserSessionHandler.require(adminToken, UserRole.ADMIN);
		UserSession session = UserSessionHandler.getSingleton().find(deleteToken);
		if (session != null) {
			UserSessionHandler.getSingleton().delete(deleteToken);
			Dao<UserSession> dao = DaoFactory.getInstance().getDao(UserSession.class);
			dao.delete(session);
		}
	}

	@Override
	public String getWebPath() {
		return "user";
	}

	public static class UserFactory implements ControlUnitFactory {

		@Override
		public Class<?> getUnitClass() {
			return User.class;
		}

		@Override
		public AbstractUnitHandler createUnitHandler(ControlCenter center) {
			return new WebUser(center);
		}

	}

}
