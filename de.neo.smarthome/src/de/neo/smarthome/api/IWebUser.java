package de.neo.smarthome.api;

import java.io.Serializable;
import java.util.ArrayList;

import de.neo.persist.DaoException;
import de.neo.remote.rmi.RemoteAble;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebField;
import de.neo.remote.web.WebGet;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.api.IControlCenter.BeanWeb;
import de.neo.smarthome.user.User;

/**
 * The web-user iterface allows access to users, authentification and
 * user-right-management.
 * 
 * @author sebastian
 *
 */
public interface IWebUser extends RemoteAble {

	/**
	 * List all users of the controlcenter
	 * 
	 * @param adminToken
	 * @return list of users
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "list", description = "List all users of the controlcenter", genericClass = BeanUser.class)
	public ArrayList<BeanUser> getUsers(@WebGet(name = "admin_token") String adminToken)
			throws RemoteException, DaoException;

	/**
	 * Creat new user
	 * 
	 * @param adminToken
	 * @param userName
	 * @param password
	 * @return new user
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "create", description = "Creat new user")
	public BeanUser createUser(@WebGet(name = "admin_token") String adminToken,
			@WebGet(name = "user_name") String userName, @WebGet(name = "password") String password)
			throws RemoteException, DaoException;

	/**
	 * Delete user
	 * 
	 * @param adminToken
	 * @param userName
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "delete", description = "Delete user")
	public void deleteUsers(@WebGet(name = "admin_token") String adminToken, @WebGet(name = "user_id") long userId)
			throws RemoteException, DaoException;

	/**
	 * Generate a user token
	 * 
	 * @param userName
	 * @param password
	 * @return user token
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(description = "Generate a user token", path = "generate_token")
	public BeanUserToken generateUserToken(@WebGet(name = "user_name") String userName,
			@WebGet(name = "password") String password) throws RemoteException, DaoException;

	/**
	 * Change password
	 * 
	 * @param token
	 * @param new_password
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(description = "Change password", path = "change_password")
	public void changePassword(@WebGet(name = "admin_token") String adminToken, @WebGet(name = "user_id") long userId,
			@WebGet(name = "new_password") String new_password) throws RemoteException, DaoException;

	/**
	 * Add unit access for user
	 * 
	 * @param adminToken
	 * @param userId
	 * @param unitId
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "add_access", description = "Add unit access for user")
	public void addUnitAccess(@WebGet(name = "admin_token") String adminToken, @WebGet(name = "user_id") long userId,
			@WebGet(name = "unit_id") String unitId) throws RemoteException, DaoException;

	/**
	 * Get list of accessible units for user
	 * 
	 * @param adminToken
	 * @param userId
	 * @return list of accessible units for user
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "get_access", description = "Get list of accessible units for user", genericClass = BeanWeb.class)
	public ArrayList<BeanWeb> getUnitAccess(@WebGet(name = "admin_token") String adminToken,
			@WebGet(name = "user_id") long userId) throws RemoteException, DaoException;

	/**
	 * Remove unit access for user
	 * 
	 * @param adminToken
	 * @param userId
	 * @param unitId
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "remove_access", description = "Remove unit access for user")
	public void removeUnitAccess(@WebGet(name = "admin_token") String adminToken, @WebGet(name = "user_id") long userId,
			@WebGet(name = "unit_id") String unitId) throws RemoteException, DaoException;

	public static class BeanUserToken implements Serializable {

		@WebField(name = "token")
		private String mToken;

		@WebField(name = "expiration")
		private Long mExpiration;

		public String getToken() {
			return mToken;
		}

		public void setToken(String token) {
			mToken = token;
		}

		public Long getExpiration() {
			return mExpiration;
		}

		public void setExpiration(Long expiration) {
			mExpiration = expiration;
		}

	}

	public static class BeanUser implements Serializable {

		@WebField(name = "name")
		private String mName;

		@WebField(name = "id")
		private long mId;

		@WebField(name = "role")
		private User.UserRole mRole;

		public String getName() {
			return mName;
		}

		public void setName(String name) {
			mName = name;
		}

		public long getId() {
			return mId;
		}

		public void setId(long id) {
			mId = id;
		}

		public User.UserRole getRole() {
			return mRole;
		}

		public void setRole(User.UserRole role) {
			mRole = role;
		}

	}

}