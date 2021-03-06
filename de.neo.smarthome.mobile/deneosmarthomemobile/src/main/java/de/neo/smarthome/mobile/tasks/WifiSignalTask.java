package de.neo.smarthome.mobile.tasks;

import java.util.HashMap;
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
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.mobile.api.IControlCenter;
import de.neo.smarthome.mobile.activities.SettingsActivity;
import de.neo.smarthome.mobile.services.RemoteService;

public class WifiSignalTask extends Thread {

	private static final int MinimalTimeGap = 1000 * 60 * 7;

	private final BlockingQueue<Runnable> mActions;
	private final AtomicBoolean mRunning = new AtomicBoolean(true);
	private RemoteService mService;
	private long mLastClientRefreshTime = 0;
	private boolean mLastClientRefeshed;
	private long mLastClientTriggerTime = 0;
	private boolean mLastClientTriggered;
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

	public void setRunning(boolean running) {
		mService.unregisterReceiver(mListener);
		mRunning.set(running);
		mActions.add(new DownloadQueue.EmptyJob());
	}

	private class RefreshJob implements Runnable {
		public void run() {
			IControlCenter cc = mService.getWebControlCenter();
			if (mLastClientRefreshTime <= System.currentTimeMillis() - MinimalTimeGap && !mLastClientRefeshed
					&& cc != null) {
				try {
					cc.getGroundPlot();
					mLastClientRefreshTime = System.currentTimeMillis();
					mLastClientRefeshed = true;
					Intent serviceIntent = new Intent(mService, RemoteService.class);
					serviceIntent.setAction(RemoteService.ACTION_UPDATE);
					mService.startService(serviceIntent);
					Log.e("wifi signal task", "success on refresh");
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

	private class TriggerJob implements Runnable {
		public void run() {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mService);
			String triggerID = preferences.getString(SettingsActivity.TRIGGER, "");
			if (triggerID != null && triggerID.length() > 0
					&& mLastClientTriggerTime <= System.currentTimeMillis() - MinimalTimeGap && !mLastClientTriggered) {
				try {
					IControlCenter cc = mService.getWebControlCenter();
					HashMap<String, Integer> result = cc.performTrigger(triggerID);
					mLastClientTriggerTime = System.currentTimeMillis();
					mLastClientTriggered = true;
					Log.e("wifi signal task",
							"success on trigger with " + result.values().iterator().next() + " events");
				} catch (RemoteException e) {
					Log.e("wifi signal task", "failure on trigger");
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
						if (!mLastClientRefeshed && mActions.size() < 50) {
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