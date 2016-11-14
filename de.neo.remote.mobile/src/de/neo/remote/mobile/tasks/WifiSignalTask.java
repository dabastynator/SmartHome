package de.neo.remote.mobile.tasks;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.neo.remote.api.PlayerException;
import de.neo.remote.api.Trigger;
import de.neo.remote.mobile.activities.SettingsActivity;
import de.neo.remote.mobile.receivers.WifiReceiver;
import de.neo.remote.mobile.services.RemoteService;
import de.neo.rmi.protokol.RemoteException;

public class WifiSignalTask extends Thread {

	private RemoteService mService;
	private long mLastClientActionTime = 0;
	private boolean mLastClientActionConnected;
	public String mCurrentSSID;

	public WifiSignalTask(RemoteService service) {
		mService = service;
		start();
	}

	@Override
	public synchronized void run() {
		while (true) {
			try {
				wait();
				tryToSendTrigger();
			} catch (InterruptedException e) {
			}
		}
	}

	private void tryToSendTrigger() {
		Trigger trigger = new Trigger();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mService);
		String triggerID = preferences.getString(SettingsActivity.TRIGGER, "");
		trigger.setTriggerID(triggerID);
		for (int i = 0; i < 10; i++) {
			try {
				String ssid = WifiReceiver.currentSSID(mService);
				if (mCurrentSSID.equals(ssid) && mLastClientActionTime <= System.currentTimeMillis() - 1000 * 60 * 7
						&& !mLastClientActionConnected) {
					if (triggerID != null && triggerID.length() > 0)
						mService.getControlCenter().trigger(trigger);
					mLastClientActionTime = System.currentTimeMillis();
					mLastClientActionConnected = true;
					try {
						mService.refreshListener();
					} catch (PlayerException | RemoteException e) {
						// ignore
					}
					return;
				}
			} catch (RemoteException e) {
			}
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {
				// Ignore
			}
		}

	}

	public void setConnection(boolean connected) {
		mLastClientActionConnected = connected;
		mCurrentSSID = WifiReceiver.currentSSID(mService);
	}

	public synchronized void wifiConnect() {
		if (!mLastClientActionConnected)
			notify();
	}

	public void wifiDisconnect() {
		if (mLastClientActionConnected) {
			mLastClientActionTime = System.currentTimeMillis();
			mLastClientActionConnected = false;
		}
	}
}