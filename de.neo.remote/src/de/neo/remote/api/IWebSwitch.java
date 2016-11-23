package de.neo.remote.api;

import java.util.ArrayList;

import de.neo.remote.api.IControlCenter.BeanWeb;
import de.neo.rmi.api.WebField;
import de.neo.rmi.api.WebGet;
import de.neo.rmi.api.WebRequest;
import de.neo.rmi.protokol.RemoteAble;
import de.neo.rmi.protokol.RemoteException;

public interface IWebSwitch extends RemoteAble {

	public enum State {
		ON, OFF
	};

	/**
	 * Return list of all switches.
	 * 
	 * @return list of all switches
	 */
	@WebRequest(path = "list", description = "List all switches of the controlcenter. A switch has an id, name, state and type.", genericClass = BeanSwitch.class)
	public ArrayList<BeanSwitch> getSwitches();

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
	public BeanSwitch setSwitchState(@WebGet(name = "id") String id, @WebGet(name = "state") String state)
			throws IllegalArgumentException, RemoteException;

	public static class BeanSwitch extends BeanWeb {

		@WebField(name = "state")
		private String mState;

		@WebField(name = "type")
		private String mType;

		public String getState() {
			return mState;
		}

		public void setState(String state) {
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
