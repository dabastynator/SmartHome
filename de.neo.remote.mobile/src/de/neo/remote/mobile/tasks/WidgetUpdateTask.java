package de.neo.remote.mobile.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import de.neo.remote.mobile.services.RemoteService;

public class WidgetUpdateTask extends Thread {

	public static int UPDATE_PERIOD = 1000 * 10;

	private boolean mRunning;
	private Context mContext;

	public WidgetUpdateTask(Context context) {
		mContext = context;
		mRunning = true;
	}

	public void setRunning(boolean running) {
		mRunning = running;
	}

	@Override
	public void run() {
		while (mRunning) {
			try {
				sleep(UPDATE_PERIOD);
				Intent serviceIntent = new Intent(mContext, RemoteService.class);
				serviceIntent.setAction(RemoteService.ACTION_UPDATE);
				mContext.startService(serviceIntent);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public boolean screenIsOn() {
		PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		if (powerManager.isScreenOn()) {
			return true;
		}
		return false;
	}

}