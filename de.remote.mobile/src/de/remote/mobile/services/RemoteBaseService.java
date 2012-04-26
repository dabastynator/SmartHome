package de.remote.mobile.services;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.RemoteViews;
import android.widget.Toast;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.ControlConstants;
import de.remote.api.IBrowser;
import de.remote.api.IChatServer;
import de.remote.api.IControl;
import de.remote.api.IPlayList;
import de.remote.api.IPlayer;
import de.remote.api.IStation;
import de.remote.api.PlayerException;
import de.remote.api.PlayingBean;
import de.remote.mobile.R;
import de.remote.mobile.activies.BrowserActivity;
import de.remote.mobile.database.ServerDatabase;
import de.remote.mobile.services.RemoteService.IRemoteActionListener;
import de.remote.mobile.services.RemoteService.MobileReceiverListener;
import de.remote.mobile.services.RemoteService.PlayerListener;
import de.remote.mobile.util.BufferBrowser;

public abstract class RemoteBaseService extends Service {

	/**
	 * id of the playing notification
	 */
	protected static final int PLAYING_NOTIFICATION_ID = 1;

	/**
	 * id of the download notification
	 */
	protected static final int DOWNLOAD_NOTIFICATION_ID = 2;

	/**
	 * port for downloads
	 */
	protected static final int DOWNLOAD_PORT = 5015;

	/**
	 * name of current server
	 */
	protected int serverID;

	/**
	 * ip of current server
	 */
	protected String serverIP;

	/**
	 * name of current server
	 */
	protected String serverName;

	/**
	 * the binder to execute all functions
	 */
	protected PlayerBinder binder;

	/**
	 * local server, to provide
	 */
	protected Server localServer;

	/**
	 * remote station object
	 */
	protected IStation station;

	/**
	 * remote browser object
	 */
	protected IBrowser browser;

	/**
	 * current selected remote player object
	 */
	protected IPlayer player;

	/**
	 * remote control object
	 */
	protected IControl control;

	/**
	 * listener for player
	 */
	protected PlayerListener playerListener;

	/**
	 * listener for download progress
	 */
	protected MobileReceiverListener progressListener;

	/**
	 * remote playlist object
	 */
	protected IPlayList playList;

	/**
	 * remote chatserver object
	 */
	protected IChatServer chatServer;

	/**
	 * current playing file
	 */
	protected PlayingBean playingFile;

	/**
	 * handler to post actions in the ui thread
	 */
	protected Handler handler = new Handler();

	/**
	 * database object to the local database
	 */
	protected ServerDatabase serverDB;

	/**
	 * list of all listeners for any action on this service
	 */
	protected List<IRemoteActionListener> actionListener = new ArrayList<IRemoteActionListener>();

	/**
	 * create connection, execute runnable if connection has started in the ui
	 * thread.
	 * 
	 * @param successRunnable
	 */
	protected void connect(final Runnable successRunnable) {
		localServer = Server.getServer();
		try {
			try {
				localServer.connectToRegistry(serverIP);
			} catch (SocketException e) {
				localServer.connectToRegistry(serverIP);
			}
			localServer.startServer();

			station = (IStation) localServer.find(ControlConstants.STATION_ID,
					IStation.class);

			if (station == null)
				throw new RemoteException(ControlConstants.STATION_ID,
						"station not found in registry");

			browser = new BufferBrowser(station.createBrowser());
			player = station.getMPlayer();
			control = station.getControl();
			playList = station.getPlayList();
			chatServer = station.getChatServer();
			registerAndUpdate();
			if (successRunnable != null)
				handler.post(successRunnable);
			handler.post(new Runnable() {
				@Override
				public void run() {
					for (IRemoteActionListener listener : actionListener)
						listener.serverConnectionChanged(serverName);
				}
			});
		} catch (final Exception e) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(RemoteBaseService.this, e.getMessage(),
							Toast.LENGTH_SHORT).show();
					for (IRemoteActionListener listener : actionListener)
						listener.serverConnectionChanged(null);
				}
			});
		}
	}

	public void registerAndUpdate() throws RemoteException, PlayerException {
		station.getMPlayer().addPlayerMessageListener(playerListener);
		station.getTotemPlayer().addPlayerMessageListener(playerListener);
		playerListener.playerMessage(player.getPlayingBean());
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
		Intent nIntent = new Intent(this, BrowserActivity.class);
		nIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		nIntent.putExtra(BrowserActivity.EXTRA_SERVER_ID, serverID);
		PendingIntent pInent = PendingIntent.getActivity(this, 0, nIntent, 0);
		notification.setLatestEventInfo(getApplicationContext(), title, body,
				pInent);
		nm.notify(PLAYING_NOTIFICATION_ID, notification);
	}

	/**
	 * create notification about downloading file
	 * 
	 * @param title
	 * @param body
	 */
	protected void makeDonwloadingNotification(String file, float progress) {
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.download, "Download started",
				System.currentTimeMillis());
		notification.contentView = new RemoteViews(getPackageName(), R.layout.download_progress);
		notification.contentView.setImageViewResource(R.id.status_icon,
				R.drawable.download);
		notification.contentView.setTextViewText(R.id.status_text,
				"download " + file);
		notification.contentView.setProgressBar(R.id.status_progress, 100,
				(int) (progress*100), false);
		Intent nIntent = new Intent(this, BrowserActivity.class);
		nIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		nIntent.putExtra(BrowserActivity.EXTRA_SERVER_ID, serverID);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, nIntent, 0);
		notification.contentIntent = pIntent;
		nm.notify(DOWNLOAD_NOTIFICATION_ID, notification);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(RemoteService.PLAYING_NOTIFICATION_ID);
		nm.cancel(RemoteService.DOWNLOAD_NOTIFICATION_ID);
		disconnect();
		serverDB.close();
	}

	/**
	 * disconnect from current connection
	 */
	protected void disconnect() {
		if (player != null) {
			try {
				player.removePlayerMessageListener(playerListener);
			} catch (Exception e) {
			}
		}
		if (localServer != null)
			try {
				localServer.close();
			} catch (IOException e) {
			}
		station = null;
		player = null;
		serverID = -1;
		serverName = null;
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(RemoteService.PLAYING_NOTIFICATION_ID);
		handler.post(new Runnable() {
			public void run() {
				for (IRemoteActionListener listener : actionListener)
					listener.serverConnectionChanged(null);
			}
		});
	}

	@Override
	public PlayerBinder onBind(Intent intent) {
		return binder;
	}

}
