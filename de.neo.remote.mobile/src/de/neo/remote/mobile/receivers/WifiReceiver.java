package de.neo.remote.mobile.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import de.neo.remote.mobile.services.RemoteService;

public class WifiReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		final String action = intent.getAction();
		if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
			NetworkInfo ni = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			if (ni != null && ni.getType() == ConnectivityManager.TYPE_WIFI && ni.isConnected()) {
				Intent i = new Intent(context, RemoteService.class);
				i.setAction(RemoteService.ACTION_WIFI_CONNECTED);
				context.startService(i);
			} else {
				// wifi connection was lost
			}
		}
	}

	public String currentSSID(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		return wifiInfo.getSSID();
	}

}
