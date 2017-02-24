package de.neo.smarthome.mobile.tasks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import de.neo.smarthome.mobile.services.RemoteService;

public class WidgetUpdateTask extends Thread {

	public static int UPDATE_PERIOD = 1000 * 10;

	private boolean mRunning;
	private Context mContext;

	private ScreenReceiver mReceiver;

	public WidgetUpdateTask(Context context) {
		mContext = context;
		mRunning = true;

		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		mReceiver = new ScreenReceiver();
		mContext.registerReceiver(mReceiver, filter);
	}

	public void setRunning(boolean running) {
		mRunning = running;
		if (!running) {
			mContext.unregisterReceiver(mReceiver);
		}
	}

	private void sendUpdateAction() {
		Intent serviceIntent = new Intent(mContext, RemoteService.class);
		serviceIntent.setAction(RemoteService.ACTION_UPDATE);
		mContext.startService(serviceIntent);
	}

	@Override
	public void run() {
		while (mRunning) {
			try {
				sleep(UPDATE_PERIOD);
				if (screenIsOn()) {
					sendUpdateAction();
				}
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

	class ScreenReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				sendUpdateAction();
			}
		}

	}

}