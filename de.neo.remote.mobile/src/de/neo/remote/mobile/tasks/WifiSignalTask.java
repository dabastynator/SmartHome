package de.neo.remote.mobile.tasks;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import de.neo.remote.api.PlayerException;
import de.neo.remote.api.Trigger;
import de.neo.remote.mobile.activities.SettingsActivity;
import de.neo.remote.mobile.services.RemoteService;
import de.neo.rmi.protokol.RemoteException;

public class WifiSignalTask extends Thread {

	private static final int MinimalTimeGap = 1000 * 60 * 7;

	private final PriorityBlockingQueue<Runnable> mActions;
	private final AtomicBoolean mRunning = new AtomicBoolean(true);
	private RemoteService mService;
	private long mLastClientActionTime = 0;
	private boolean mLastClientActionConnected;
	public String mCurrentSSID;
	private NetworkListener mListener;

	public WifiSignalTask(RemoteService service) {
		mActions = new PriorityBlockingQueue<>(5);
		mService = service;
		mListener = new NetworkListener();
		service.registerReceiver(mListener, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
	}

	@Override
	public void run() {
		while (mRunning.get()) {
			Runnable job;
			try {
				job = mActions.take();
				job.run();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void end() {
		mService.unregisterReceiver(mListener);
		mRunning.set(false);
	}

	private class NetworkJob implements Runnable, Comparable<NetworkJob> {
		public void run() {
			Trigger trigger = new Trigger();
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mService);
			String triggerID = preferences.getString(SettingsActivity.TRIGGER, "");
			trigger.setTriggerID(triggerID);
			for (int i = 0; i < 10; i++) {
				try {
					String ssid = currentSSID(mService);
					if (mCurrentSSID.equals(ssid)
							&& mLastClientActionTime <= System.currentTimeMillis() - MinimalTimeGap
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

		@Override
		public int compareTo(NetworkJob another) {
			return 0;
		}

	}

	public void setConnection(boolean connected) {
		mLastClientActionConnected = connected;
		mCurrentSSID = currentSSID(mService);
	}

	public static String currentSSID(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		return wifiInfo.getSSID();
	}

	private class NetworkListener extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
				NetworkInfo ni = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				if (ni != null && ni.getType() == ConnectivityManager.TYPE_WIFI) {
					if (ni.isConnected()) {
						if (!mLastClientActionConnected && mService.getControlCenter() != null && mActions.size() < 2)
							mActions.add(new NetworkJob());
					} else {
						if (mLastClientActionConnected) {
							mLastClientActionTime = System.currentTimeMillis();
							mLastClientActionConnected = false;
						}
					}
				}
			}
		}

	}
}