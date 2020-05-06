package de.neo.smarthome.user;

import java.util.ArrayList;

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

	private User userById(long userId) throws DaoException, RemoteException {
		Dao<User> userDao = DaoFactory.getInstance().getDao(User.class);
		User user = userDao.loadById(userId);
		if (user != null) {
			return user;
		}
		throw new RemoteException("Unknown user-id: " + userId);
	}

	@WebRequest(path = "list", description = "List all users of the controlcenter", genericClass = BeanUser.class)
	public ArrayList<BeanUser> getUsers(@WebGet(name = "admin_token") String adminToken)
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
	public BeanUser createUser(@WebGet(name = "admin_token") String adminToken,
			@WebGet(name = "user_name") String userName, @WebGet(name = "password") String password)
			throws RemoteException, DaoException {
		UserSessionHandler.require(adminToken, UserRole.ADMIN);
		User user = new User();
		user.setName(userName);
		user.setPassword(password);
		Dao<User> userDao = DaoFactory.getInstance().getDao(User.class);
		userDao.save(user);
		RemoteLogger.performLog(LogPriority.INFORMATION, "Create new user " + user.getName(), "UserHandler");
		return toBean(user);
	}

	@WebRequest(path = "delete", description = "Delete user")
	public void deleteUsers(@WebGet(name = "admin_token") String adminToken, @WebGet(name = "user_id") long userId)
			throws RemoteException, DaoException {
		UserSessionHandler.require(adminToken, UserRole.ADMIN);
		User user = userById(userId);
		Dao<User> userDao = DaoFactory.getInstance().getDao(User.class);
		RemoteLogger.performLog(LogPriority.INFORMATION, "Delete user " + user.getName(), "UserHandler");
		userDao.delete(user);
	}

	@WebRequest(description = "Generate a user token", path = "generate_token")
	public BeanUserToken generateUserToken(@WebGet(name = "user_name") String userName,
			@WebGet(name = "password") String password) throws RemoteException, DaoException {
		Dao<User> userDao = DaoFactory.getInstance().getDao(User.class);
		for (User user : userDao.loadAll()) {
			if (user.getName().equals(userName) && user.getPassword().equals(password)) {
				Long expiration = System.currentTimeMillis() + UserSessionHandler.DEFALT_DURATION;
				UserSession session = UserSessionHandler.getSingleton().generate(user, expiration);
				BeanUserToken token = new BeanUserToken();
				token.setToken(session.getToken());
				token.setExpiration(session.getExpiration());
				return token;
			}
		}
		throw new RemoteException("Invalid username or password");
	}

	@WebRequest(description = "Change password", path = "change_password")
	public void changePassword(@WebGet(name = "admin_token") String adminToken, @WebGet(name = "user_id") long userId,
			@WebGet(name = "new_password") String new_password) throws RemoteException, DaoException {
		UserSessionHandler.require(adminToken, UserRole.ADMIN);
		User user = userById(userId);
		user.setPassword(new_password);
		Dao<User> userDao = DaoFactory.getInstance().getDao(User.class);
		userDao.update(user);
		RemoteLogger.performLog(LogPriority.INFORMATION, "Change password for user " + user.getName(), "UserHandler");
	}

	@WebRequest(path = "add_access", description = "Add unit access for user")
	public void addUnitAccess(@WebGet(name = "admin_token") String adminToken, @WebGet(name = "user_id") long userId,
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
	public ArrayList<BeanWeb> getUnitAccess(@WebGet(name = "admin_token") String adminToken,
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
	public void removeUnitAccess(@WebGet(name = "admin_token") String adminToken, @WebGet(name = "user_id") long userId,
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
