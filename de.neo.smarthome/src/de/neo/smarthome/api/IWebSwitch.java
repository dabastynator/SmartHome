package de.neo.smarthome.api;

import java.io.IOException;
import java.util.ArrayList;

import de.neo.persist.DaoException;
import de.neo.remote.rmi.RemoteAble;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebField;
import de.neo.remote.web.WebGet;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.api.IControlCenter.BeanWeb;

public interface IWebSwitch extends RemoteAble {

	public enum State {
		ON, OFF
	};

	/**
	 * Return list of all switches.
	 * 
	 * @param token
	 * @return list of all switches
	 */
	@WebRequest(path = "list", description = "List all switches of the controlcenter. A switch has an id, name, state and type.", genericClass = BeanSwitch.class)
	public ArrayList<BeanSwitch> getSwitches(@WebGet(name = "token") String token) throws RemoteException;

	/**
	 * Set the state of switch with specified id.
	 * 
	 * @param id
	 * @param state
	 * @return state of switch
	 * @throws IllegalArgumentException
	 * @throws RemoteException
	 */
	@WebRequest(description = "Set the state of switch with specified id. State must be [ON|OFF].", path = "set")
	public BeanSwitch setSwitchState(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "state") String state) throws IllegalArgumentException, RemoteException;

	/**
	 * Create new switch
	 * 
	 * @param token
	 * @param id
	 * @param name
	 * @param familyCode
	 * @param switchNumber
	 * @param description
	 * @param x
	 * @param y
	 * @param z
	 * @return New switch
	 * @throws RemoteException
	 * @throws IOException
	 * @throws DaoException
	 */
	@WebRequest(path = "create", description = "Create new switch.")
	public BeanSwitch create(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "name") String name, @WebGet(name = "family_code") String familyCode,
			@WebGet(name = "switch_number") int switchNumber, @WebGet(name = "description") String description,
			@WebGet(name = "x") float x, @WebGet(name = "y") float y, @WebGet(name = "z") float z)
			throws RemoteException, IOException, DaoException;

	/**
	 * Update existing switch
	 * 
	 * @param token
	 * @param id
	 * @param name
	 * @param familyCode
	 * @param switchNumber
	 * @param description
	 * @param x
	 * @param y
	 * @param z
	 * @return Updated switch
	 * @throws RemoteException
	 * @throws IOException
	 * @throws DaoException
	 */
	@WebRequest(path = "update", description = "Update existing switch.")
	public BeanSwitch update(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "name") String name, @WebGet(name = "family_code") String familyCode,
			@WebGet(name = "switch_number") int switchNumber, @WebGet(name = "description") String description,
			@WebGet(name = "x") float x, @WebGet(name = "y") float y, @WebGet(name = "z") float z)
			throws RemoteException, IOException, DaoException;

	/**
	 * Delete switch
	 * 
	 * @param token
	 * @param id
	 * @throws RemoteException
	 * @throws IOException
	 * @throws DaoException
	 */
	@WebRequest(path = "delete", description = "Delete switch.")
	public void delete(@WebGet(name = "token") String token, @WebGet(name = "id") String id)
			throws RemoteException, IOException, DaoException;

	public static class BeanSwitch extends BeanWeb {

		@WebField(name = "state")
		private State mState;

		@WebField(name = "type")
		private String mType;

		public State getState() {
			return mState;
		}

		public void setState(State state) {
			mState = state;
		}

		public String getType() {
			return mType;
		}

		public void setType(String type) {
			mType = type;
		}

	}

}
