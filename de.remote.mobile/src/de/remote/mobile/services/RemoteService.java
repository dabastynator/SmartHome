package de.remote.mobile.services;

import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;
import de.newsystem.rmi.api.RMILogger;
import de.newsystem.rmi.api.RMILogger.LogPriority;
import de.newsystem.rmi.api.RMILogger.RMILogListener;
import de.newsystem.rmi.transceiver.ReceiverProgress;
import de.remote.api.IPlayerListener;
import de.remote.api.PlayingBean;
import de.remote.mobile.database.ServerDatabase;
import de.remote.mobile.receivers.WLANReceiver;
import de.remote.mobile.util.NotificationHandler;

/**
 * service for remotecontrol a server. the binder enables functions to control
 * all functions on the server.
 * 
 * @author sebastian
 */
public class RemoteService extends RemoteBaseService {

	private WLANReceiver wlanReceiver;
	

	@Override
	public void onCreate() {
		super.onCreate();
		binder = new PlayerBinder(this);
		notificationHandler = new NotificationHandler(this);
		playerListener = new PlayerListener();
		downloadListener = new ProgressListener();
		actionListener.add(notificationHandler);
		serverDB = new ServerDatabase(this);
		wlanReceiver = new WLANReceiver(this);
		registerReceiver(wlanReceiver, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}

	public void onDestroy() {
		unregisterReceiver(wlanReceiver);
		super.onDestroy();
	};

	/**
	 * listener for player activity. make notification if any message comes.
	 * 
	 * @author sebastian
	 */
	public class PlayerListener implements IPlayerListener {

		@Override
		public void playerMessage(final PlayingBean playing) {
			playingFile = playing;
			handler.post(new Runnable() {
				@Override
				public void run() {
					for (IRemoteActionListener listener : actionListener)
						listener.onPlayingBeanChanged(playing);
				}
			});
		}
	}
	
	public class ProgressListener implements ReceiverProgress{

		@Override
		public void startReceive(final long size) {
			handler.post(new Runnable() {
				public void run() {
					for (IRemoteActionListener l : actionListener)
						l.startReceive(size);
				}
			});			
		}

		@Override
		public void progressReceive(final long size) {
			handler.post(new Runnable() {
				public void run() {
					for (IRemoteActionListener l : actionListener)
						l.progressReceive(size);
				}
			});			
		}

		@Override
		public void endReceive(final long size) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(RemoteService.this, "download finished", Toast.LENGTH_SHORT)
							.show();
					for (IRemoteActionListener l : actionListener)
						l.endReceive(size);
				}
			});			
		}

		@Override
		public void exceptionOccurred(final Exception e) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(RemoteService.this,
							"error occurred while loading: " + e.getMessage(),
							Toast.LENGTH_SHORT).show();
					for (IRemoteActionListener l : actionListener)
						l.exceptionOccurred(e);
				}
			});			
		}

		@Override
		public void downloadCanceled() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(RemoteService.this, "download cancled", Toast.LENGTH_SHORT)
							.show();
					for (IRemoteActionListener l : actionListener)
						l.downloadCanceled();
				}
			});			
		}
		
	}

	

	/**
	 * this interface informs listener about any action on the remote service,
	 * such as new connection or new playing file.
	 * 
	 * @author sebastian
	 */
	public interface IRemoteActionListener extends ReceiverProgress {

		/**
		 * server player plays new file
		 * 
		 * @param bean
		 */
		void onPlayingBeanChanged(PlayingBean bean);

		/**
		 * connection with server changed
		 * 
		 * @param serverName
		 */
		void onServerConnectionChanged(String serverName, int serverID);

		/**
		 * call on stopping remote service.
		 */
		void onStopService();

	}
}
