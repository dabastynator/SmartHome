package de.neo.remote.mobile.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import de.neo.remote.mobile.services.RemoteService;

/**
 * this receiver gets information about wlan connections, to update about new
 * playing file and register for listener.
 * 
 * @author sebastian
 */
public class WLANReceiver extends BroadcastReceiver {

	/**
	 * action for connection with wlan
	 */
	public static final String WLAN_RECONNECT = "wlan_reconnect";

	/**
	 * the service to inform about new wlan connection
	 */
	private RemoteService service;

	/**
	 * allocate new wlan receiver
	 */
	public WLANReceiver(RemoteService remoteBaseService) {
		this.service = remoteBaseService;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		WifiManager myWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		if (myWifiManager.isWifiEnabled() && service.onBind(null).isConnected()
				&& !service.isRestricted()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					service.refreshControlCenter();
				}
			}).start();
		}
	}

}
