package de.remote.mobile.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import de.remote.api.PlayingBean.STATE;
import de.remote.mobile.R;
import de.remote.mobile.activities.BrowserActivity;
import de.remote.mobile.database.ServerDatabase;
import de.remote.mobile.receivers.WLANReceiver;

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
		playerListener = new PlayerListener();
		actionListener.add(new NotificationHandler());
		progressListener = new MobileReceiverListener();
		serverDB = new ServerDatabase(this);
		wlanReceiver = new WLANReceiver(this);
		registerReceiver(wlanReceiver, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		RMILogger.addLogListener(new RMILogListener() {
			@Override
			public void rmiLog(LogPriority priority, String message, String id,
					long date) {
				Log.e("RMI Logs", message);
			}
		});
	}

	public void onDestroy() {
		unregisterReceiver(wlanReceiver);
		super.onDestroy();
	};

	/**
	 * the receiver gets information about the download progress and informs via
	 * a notification.
	 * 
	 * @author sebastian
	 */
	public class MobileReceiverListener implements ReceiverProgress {

		/**
		 * current downloading file
		 */
		private String file;

		/**
		 * size of the whole file
		 */
		private long fullSize;

		/**
		 * set new donwloading file
		 * 
		 * @param file
		 */
		public void setFile(String file) {
			this.file = file;
		}

		@Override
		public void startReceive(final long size) {
			fullSize = size;
			makeDonwloadingNotification(file, 0);
			handler.post(new Runnable() {
				public void run() {
					for (IRemoteActionListener l : actionListener)
						l.startReceive(size);
				}
			});
		}

		@Override
		public void progressReceive(final long size) {
			makeDonwloadingNotification(file, ((float) size)
					/ ((float) fullSize));
			handler.post(new Runnable() {
				public void run() {
					for (IRemoteActionListener l : actionListener)
						l.progressReceive(size);
				}
			});
		}

		@Override
		public void endReceive(final long size) {
			NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(RemoteService.DOWNLOAD_NOTIFICATION_ID);
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(RemoteService.this, file + " loaded",
							Toast.LENGTH_SHORT).show();
					for (IRemoteActionListener l : actionListener)
						l.endReceive(size);
				}
			});
		}

		@Override
		public void exceptionOccurred(final Exception e) {
			NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(RemoteService.DOWNLOAD_NOTIFICATION_ID);
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
			NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(RemoteService.DOWNLOAD_NOTIFICATION_ID);
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(RemoteService.this, "download cancled",
							Toast.LENGTH_SHORT).show();
					for (IRemoteActionListener l : actionListener)
						l.downloadCanceled();
				}
			});
		}

	}

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
	
	public class NotificationHandler implements IRemoteActionListener{

		@Override
		public void startReceive(long size) {
		}

		@Override
		public void progressReceive(long size) {
		}

		@Override
		public void endReceive(long size) {
		}

		@Override
		public void exceptionOccurred(Exception e) {
		}

		@Override
		public void downloadCanceled() {
		}

		@Override
		public void onPlayingBeanChanged(PlayingBean playing) {
			if (playing == null)
				return;
			StringBuilder sb = new StringBuilder();
			String t = "Playing";
			if (playing.getTitle() != null && playing.getTitle().length() > 0)
				t = playing.getTitle();
			else if (playing.getFile() != null)
				t = playing.getFile();
			if (playing.getArtist() != null && playing.getArtist().length() > 0)
				sb.append(playing.getArtist());
			if (playing.getAlbum() != null && playing.getAlbum().length() > 0)
				sb.append(" <" + playing.getAlbum() + ">");
			final String msg = sb.toString();
			final String title = t;
			if (playing.getState() == STATE.DOWN) {
				NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				nm.cancel(RemoteService.PLAYING_NOTIFICATION_ID);
			} else
				makePlayingNotification(title, msg);
		}
		
		/**
		 * create notification about playing file
		 * 
		 * @param title
		 * @param body
		 */
		protected void makePlayingNotification(String title, String body) {
			NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			int icon = R.drawable.browser;
			Notification notification = new Notification(icon, "Player started",
					System.currentTimeMillis());
			Intent nIntent = new Intent(RemoteService.this, BrowserActivity.class);
			nIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			nIntent.putExtra(BrowserActivity.EXTRA_SERVER_ID, serverID);
			PendingIntent pInent = PendingIntent.getActivity(RemoteService.this, 0, nIntent, 0);
			notification.setLatestEventInfo(getApplicationContext(), title, body,
					pInent);
			nm.notify(PLAYING_NOTIFICATION_ID, notification);
		}

		@Override
		public void onServerConnectionChanged(String serverName) {
		}

		@Override
		public void onStopService() {
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
		void onServerConnectionChanged(String serverName);

		/**
		 * call on stopping remote service.
		 */
		void onStopService();

	}
}
