package de.neo.remote.mobile.tasks;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
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
import android.util.Log;
import de.neo.remote.api.Trigger;
import de.neo.remote.mobile.activities.SettingsActivity;
import de.neo.remote.mobile.services.RemoteService;
import de.neo.rmi.protokol.RemoteException;

public class WifiSignalTask extends Thread {

	private static final int MinimalTimeGap = 1000 * 60 * 7;

	private final BlockingQueue<Runnable> mActions;
	private final AtomicBoolean mRunning = new AtomicBoolean(true);
	private RemoteService mService;
	private long mLastClientRefreshTime = 0;
	private boolean mLastClientRefeshed;
	private long mLastClientTriggerTime = 0;
	private boolean mLastClientTriggered;
	public String mCurrentSSID;
	private NetworkListener mListener;

	public WifiSignalTask(RemoteService service) {
		mActions = new LinkedBlockingDeque<>();
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

	private class RefreshJob implements Runnable {
		public void run() {
			try {
				String ssid = currentSSID(mService);
				if (mCurrentSSID.equals(ssid) && mLastClientRefreshTime <= System.currentTimeMillis() - MinimalTimeGap
						&& !mLastClientRefeshed) {
					mService.refreshListener();
					mLastClientRefreshTime = System.currentTimeMillis();
					mLastClientRefeshed = true;
					Log.e("wifi signal task", "success on refresh");
				}
			} catch (RemoteException e) {
				Log.e("wifi signal task", "failure on refresh");
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					// Ignore
				}
			}
		}

	}

	private class TriggerJob implements Runnable {
		public void run() {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mService);
			String triggerID = preferences.getString(SettingsActivity.TRIGGER, "");
			String ssid = currentSSID(mService);
			if (triggerID != null && triggerID.length() > 0 && mCurrentSSID.equals(ssid)
					&& mLastClientTriggerTime <= System.currentTimeMillis() - MinimalTimeGap && !mLastClientTriggered) {
				Trigger trigger = new Trigger();
				trigger.setTriggerID(triggerID);
				try {

					mService.getControlCenter().trigger(trigger);
					mLastClientTriggerTime = System.currentTimeMillis();
					mLastClientTriggered = true;
					Log.e("wifi signal task", "success on trigger");
				} catch (RemoteException e) {
					Log.e("wifi signal task", "failure on refresh");
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}

	}

	public void setConnection(boolean connected) {
		mLastClientRefeshed = connected;
		mLastClientTriggered = connected;
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
						Log.e("wifi signal task", "wifi connected");
						if (!mLastClientRefeshed && mService.getControlCenter() != null && mActions.size() < 50) {
							for (int i = 0; i < 10; i++) {
								mActions.add(new RefreshJob());
								mActions.add(new TriggerJob());
							}
						}
					} else {
						Log.e("wifi signal task", "wifi disconnected");
						if (mLastClientRefeshed) {
							mLastClientRefreshTime = System.currentTimeMillis();
							mLastClientRefeshed = false;
						}
						if (mLastClientTriggered) {
							mLastClientTriggerTime = System.currentTimeMillis();
							mLastClientTriggered = false;
						}
					}
				}
			}
		}

	}
}