package de.neo.smarthome.gpio;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.api.IWebSwitch.State;
import de.neo.smarthome.gpio.SerialReader.ISerialListener;

public class InternetSwitch implements ISerialListener {

	public static final String ROOT = "InternetSwitch";

	public static final String FAMILY_REGEX = "[01]{5}";

	private GPIOSender mPower;
	private String mType;
	private String mFamilyCode;
	private int mSwitchNumber;
	private State mState;
	private boolean mReadOnly;
	private String mId;
	private GPIOControlUnit mUnit;

	public InternetSwitch(GPIOSender power, GPIOControlUnit unit) {
		mPower = power;
		mState = State.OFF;
		mUnit = unit;
		SerialReader.getInstance().getListener().add(this);
	}

	public void initialize(Element element) throws SAXException {
		for (String attribute : new String[] { "readonly", "familyCode", "type", "switchNumber" })
			if (!element.hasAttribute(attribute))
				throw new SAXException(attribute + " missing for internet switch");
		mFamilyCode = element.getAttribute("familyCode");
		mSwitchNumber = Integer.parseInt(element.getAttribute("switchNumber"));
		mType = element.getAttribute("type");
		String readonly = element.getAttribute("readonly");
		mReadOnly = readonly.equals("1") || readonly.equals("true");
		mId = element.getAttribute("id");
		if (!mFamilyCode.matches(FAMILY_REGEX))
			throw new IllegalArgumentException("Invalid Familycode: " + mFamilyCode + ". must match " + FAMILY_REGEX);

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

	public State getState() throws RemoteException {
		return mState;
	}

	private void informListener(State state) {
		Map<String, String> parameterExchange = new HashMap<String, String>();
		parameterExchange.put("@state", state.toString().toLowerCase());
		mUnit.fireTrigger(parameterExchange, "@state=" + state.toString().toLowerCase());
	}

	public String getType() {
		return mType;
	}

	public boolean isReadOnly() throws RemoteException {
		return mReadOnly;
	}

	@Override
	public void OnSwitchEvent(String family, int switchNumber, State state) {
		if (mFamilyCode.equals(family) && mSwitchNumber == switchNumber) {
			mState = state;
			informListener(state);
		}
	}

}
