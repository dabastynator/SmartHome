package de.neo.smarthome.api;

import java.io.IOException;
import java.util.ArrayList;

import de.neo.persist.DaoException;
import de.neo.remote.rmi.RemoteAble;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebField;
import de.neo.remote.web.WebParam;
import de.neo.remote.web.WebRequest;

public interface IWebSwitch extends RemoteAble {

	public enum State {
		ON, OFF
	};

	@WebRequest(path = "list", description = "List all switches of the controlcenter. A switch has an id, name, state and type.", genericClass = BeanSwitch.class)
	public ArrayList<BeanSwitch> getSwitches(
			@WebParam(name = "token") String token)
					throws RemoteException;

	@WebRequest(description = "Set the state of switch with specified id. State must be [ON|OFF].", path = "set")
	public BeanSwitch setSwitchState(
			@WebParam(name = "token") String token, 
			@WebParam(name = "id") String id,
			@WebParam(name = "state") String state)
					throws IllegalArgumentException, RemoteException;

	@WebRequest(path = "create", description = "Create new switch.")
	public BeanSwitch create(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "name") String name, 
			@WebParam(name = "family_code") String familyCode,
			@WebParam(name = "switch_number") int switchNumber, 
			@WebParam(name = "description") String description)
					throws RemoteException, IOException, DaoException;

	@WebRequest(path = "update", description = "Update existing switch.")
	public BeanSwitch update(
			@WebParam(name = "token") String token, 
			@WebParam(name = "id") String id,
			@WebParam(name = "name") String name, 
			@WebParam(name = "family_code") String familyCode,
			@WebParam(name = "switch_number") int switchNumber, 
			@WebParam(name = "description") String description)
					throws RemoteException, IOException, DaoException;

	@WebRequest(path = "delete", description = "Delete switch.")
	public void delete(
			@WebParam(name = "token") String token, 
			@WebParam(name = "id") String id)
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
