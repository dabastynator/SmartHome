package de.remote.mobile.services;

import android.app.NotificationManager;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.widget.Toast;
import de.newsystem.rmi.transceiver.ReceiverProgress;
import de.remote.api.IPlayerListener;
import de.remote.api.PlayingBean;
import de.remote.api.PlayingBean.STATE;
import de.remote.mobile.database.ServerDatabase;
import de.remote.mobile.receivers.WLANReceiver;

/**
 * service for remotecontrol a server. the binder enables functions to control
 * all functions on the server.
 * 
 * @author sebastian
 */
public class RemoteService extends RemoteBaseService {

	@Override
	public void onCreate() {
		super.onCreate();
		binder = new PlayerBinder(this);
		playerListener = new PlayerListener();
		progressListener = new MobileReceiverListener();
		serverDB = new ServerDatabase(this);
		registerReceiver(new WLANReceiver(this), new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}

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
		public void startReceive(long size) {
			fullSize = size;
			makeDonwloadingNotification(file, 0);
		}

		@Override
		public void progressReceive(long size) {
			System.out.println(size);
			makeDonwloadingNotification(file, ((float)size)/((float)fullSize));
		}

		@Override
		public void endReceive(long size) {
			NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(RemoteService.DOWNLOAD_NOTIFICATION_ID);
			handler.post(new Runnable(){
				@Override
				public void run() {
					Toast.makeText(RemoteService.this, file + " loaded", Toast.LENGTH_SHORT).show();
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
			handler.post(new Runnable() {
				@Override
				public void run() {
					if (playing.getState() == STATE.DOWN) {
						NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
						nm.cancel(RemoteService.PLAYING_NOTIFICATION_ID);
					} else
						makePlayingNotification(title, msg);
					for (IRemoteActionListener listener : actionListener)
						listener.newPlayingFile(playing);
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
	public interface IRemoteActionListener {

		/**
		 * server player plays new file
		 * 
		 * @param bean
		 */
		void newPlayingFile(PlayingBean bean);

		/**
		 * connection with server changed
		 * 
		 * @param serverName
		 */
		void serverConnectionChanged(String serverName);

	}
}
