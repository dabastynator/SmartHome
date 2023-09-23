package de.neo.smarthome.switches;

import java.util.HashMap;
import java.util.Map;

import de.neo.persist.annotations.Domain;
import de.neo.persist.annotations.Persist;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebProxyBuilder;
import de.neo.smarthome.api.Event;
import de.neo.smarthome.api.IWebSwitch.State;
import de.neo.smarthome.switches.WebSwitchImpl.SwitchUnit;

/**
 * @author sebastian
 *
 */
@Domain(name = "HassSwitch")
public class HassSwitchUnit extends SwitchUnit {

	@Persist(name = "entityId")
	private String mEntityId;

	private State mState = State.OFF;

	private HassAPI mHassAPI;

	private String mAuth;

	private HassAPI getHassAPI() {
		if (mHassAPI == null) {
			mHassAPI = new WebProxyBuilder().setEndPoint(mCenter.getHassUrl()).setInterface(HassAPI.class).create();
			mAuth = "Bearer " + mCenter.getHassToken();
		}
		return mHassAPI;
	}

	public String getEntityId() {
		return mEntityId;
	}
	
	@Override
	public boolean performEvent(Event event) throws RemoteException, EventException {
		String state = event.getParameter("state");
		if (state == null)
			throw new EventException("Parameter state (on|off) missing to execute switch event!");
		if (state.equalsIgnoreCase("on"))
			setState(State.ON);
		else if (state.equalsIgnoreCase("off"))
			setState(State.OFF);
		else
			throw new EventException("Unknown parameter-value for switch-event '" + state + "'! Excpected: on|off");
		return true;
	}

	public void setState(final State state) throws RemoteException {
		if (mState != state) {
			String type = mEntityId.substring(0, 6);
			String setTo = "off";
			if ("light.".equals(type)) {
				type = "light";
			} else {
				type = "switch";
			}
			if (state == State.ON) {
				setTo = "on";
			}
			getHassAPI().setState(mAuth, mEntityId, type, setTo);
			this.mState = state;
			new Thread() {
				public void run() {
					informListener(state);
				};
			}.start();
		}
	}
	
	public State getState() {
		return mState;
	}

	private void informListener(State state) {
		Map<String, String> parameterExchange = new HashMap<String, String>();
		parameterExchange.put("@state", state.toString().toLowerCase());
		fireTrigger(parameterExchange, "@state=" + state.toString().toLowerCase());
	}

	public void setStateIntern(String state) {
		mState = State.OFF;
		if ("on".equals(state)) {
			mState = State.ON;
		}
	}

}
