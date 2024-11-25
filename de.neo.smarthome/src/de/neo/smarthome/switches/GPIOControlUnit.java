package de.neo.smarthome.switches;

import de.neo.persist.annotations.Domain;
import de.neo.persist.annotations.Persist;
import de.neo.remote.rmi.RemoteException;
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

	public void setState(final State state) throws RemoteException {
		if (mState != state) {
			mPower.setSwitchState(mFamilyCode, mSwitchNumber, state);
			this.mState = state;
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

	public boolean isReadOnly() throws RemoteException {
		return mReadOnly;
	}

}
