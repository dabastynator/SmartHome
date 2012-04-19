package de.remote.mobile.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.PlayerException;
import de.remote.mobile.services.RemoteService;

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
	public WLANReceiver(RemoteService service) {
		this.service = service;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		WifiManager myWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		if (myWifiManager.isWifiEnabled() && service.onBind(null).isConnected() && !service.isRestricted()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						service.registerAndUpdate();
					} catch (RemoteException e) {
					} catch (PlayerException e) {
					}
				}
			}).start();
		}
	}

}
