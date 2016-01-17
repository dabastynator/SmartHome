package de.neo.remote.mobile.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import de.neo.remote.mobile.services.RemoteService;

public class WifiReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		final String action = intent.getAction();
		if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
			if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
				Intent i = new Intent(context, RemoteService.class);
				i.setAction(RemoteService.ACTION_WIFI_CONNECTED);
				context.startService(i);
			} else {
				// wifi connection was lost
			}
		}
	}

}
