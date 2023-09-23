package de.neo.smarthome.switches;

import java.util.HashMap;
import java.util.Map;

import de.neo.persist.annotations.Domain;
import de.neo.persist.annotations.Persist;
import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.api.Event;
import de.neo.smarthome.api.IWebSwitch.State;
import de.neo.smarthome.switches.WebSwitchImpl.SwitchUnit;

/**
 * @author sebastian
 *
 */
@Domain(name = "InternetSwitch")
public class GPIOControlUnit extends SwitchUnit {

	public static final String FAMILY_REGEX = "[01]{5}";

	@Persist(name = "familyCode")
	private String mFamilyCode;

	@Persist(name = "switchNumber")
	private int mSwitchNumber;

	private State mState = State.OFF;

	@Persist(name = "readOnly")
	private boolean mReadOnly;

	private GPIOSender mPower = GPIOSender.getInstance();

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
			mPower.setSwitchState(mFamilyCode, mSwitchNumber, state);
			this.mState = state;
			new Thread() {
				public void run() {
					informListener(state);
				};
			}.start();
		}
	}
	
	public String getFamilyCode() {
		return mFamilyCode;
	}

	public void setFamilyCode(String familyCode) {
		mFamilyCode = familyCode;
	}

	public int getSwitchNumber() {
		return mSwitchNumber;
	}

	public void setSwitchNumber(int switchNumber) {
		mSwitchNumber = switchNumber;
	}

	public State getState() {
		return mState;
	}

	private void informListener(State state) {
		Map<String, String> parameterExchange = new HashMap<String, String>();
		parameterExchange.put("@state", state.toString().toLowerCase());
		fireTrigger(parameterExchange, "@state=" + state.toString().toLowerCase());
	}

	public boolean isReadOnly() throws RemoteException {
		return mReadOnly;
	}

}
