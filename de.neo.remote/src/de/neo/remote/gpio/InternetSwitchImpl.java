package de.neo.remote.gpio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.neo.remote.api.IInternetSwitch;
import de.neo.remote.api.IInternetSwitchListener;
import de.neo.remote.gpio.SerialReader.ISerialListener;
import de.neo.rmi.protokol.RemoteException;

public class InternetSwitchImpl implements IInternetSwitch, ISerialListener {

	public static final String ROOT = "InternetSwitch";

	public static final String FAMILY_REGEX = "[01]{5}";

	/**
	 * Listener for power switch change.
	 */
	private List<IInternetSwitchListener> mListeners = Collections
			.synchronizedList(new ArrayList<IInternetSwitchListener>());
	private SwitchPower mPower;
	private String mType;
	private String mFamilyCode;
	private int mSwitchNumber;
	private State mState;
	private boolean mReadOnly;
	private String mId;

	public InternetSwitchImpl(SwitchPower power) {
		mPower = power;
		mState = State.OFF;
		SerialReader.getInstance().getListener().add(this);
	}

	public void initialize(Element element) throws SAXException {
		for (String attribute : new String[] { "readonly", "familyCode",
				"type", "switchNumber" })
			if (!element.hasAttribute(attribute))
				throw new SAXException(attribute
						+ " missing for internet switch");
		mFamilyCode = element.getAttribute("familyCode");
		mSwitchNumber = Integer.parseInt(element.getAttribute("switchNumber"));
		mType = element.getAttribute("type");
		String readonly = element.getAttribute("readonly");
		mReadOnly = readonly.equals("1") || readonly.equals("true");
		mId = element.getAttribute("id");
		if (!mFamilyCode.matches(FAMILY_REGEX))
			throw new IllegalArgumentException("Invalid Familycode: "
					+ mFamilyCode + ". must match " + FAMILY_REGEX);

	}

	@Override
	public void setState(final State state) throws RemoteException {
		mPower.setSwitchState(mFamilyCode, mSwitchNumber, state);
		this.mState = state;
		new Thread() {
			public void run() {
				informListener(state);
			};
		}.start();
	}

	@Override
	public State getState() throws RemoteException {
		return mState;
	}

	@Override
	public void registerPowerSwitchListener(IInternetSwitchListener listener)
			throws RemoteException {
		if (!mListeners.contains(listener))
			mListeners.add(listener);
	}

	@Override
	public void unregisterPowerSwitchListener(IInternetSwitchListener listener)
			throws RemoteException {
		mListeners.remove(listener);
	}

	private void informListener(State state) {
		List<IInternetSwitchListener> exceptionList = new ArrayList<IInternetSwitchListener>();
		for (IInternetSwitchListener listener : mListeners) {
			try {
				listener.onPowerSwitchChange(mId, state);
			} catch (RemoteException e) {
				exceptionList.add(listener);
			}
		}
		mListeners.removeAll(exceptionList);
	}

	@Override
	public String getType() {
		return mType;
	}

	@Override
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
