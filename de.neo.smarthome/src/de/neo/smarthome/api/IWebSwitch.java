package de.neo.smarthome.api;

import java.util.ArrayList;

import de.neo.remote.rmi.RemoteAble;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebField;
import de.neo.remote.web.WebGet;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.controlcenter.IControlCenter.BeanWeb;

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
	public ArrayList<BeanSwitch> getSwitches() throws RemoteException;

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
