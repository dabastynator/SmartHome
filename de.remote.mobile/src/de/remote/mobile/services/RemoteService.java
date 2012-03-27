package de.remote.mobile.services;

import java.io.IOException;
import java.net.SocketException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.ControlConstants;
import de.remote.api.IBrowser;
import de.remote.api.IChatServer;
import de.remote.api.IControl;
import de.remote.api.IPlayList;
import de.remote.api.IPlayer;
import de.remote.api.IPlayerListener;
import de.remote.api.IStation;
import de.remote.api.PlayingBean;
import de.remote.api.PlayingBean.STATE;
import de.remote.mobile.R;
import de.remote.mobile.activies.BrowserActivity;
import de.remote.mobile.database.ServerDatabase;
import de.remote.mobile.util.BufferBrowser;

/**
 * service for remotecontrol an server. the binder enables functions to control
 * all functions on the server.
 * 
 * @author sebastian
 */
public class RemoteService extends Service {

	/**
	 * id of the notification
	 */
	public static final int NOTIFICATION_ID = 1;

	/**
	 * name of current server
	 */
	private String serverName;

	/**
	 * ip of current server
	 */
	private String serverIP;

	/**
	 * the binder to execute all functions
	 */
	private PlayerBinder binder;

	/**
	 * local server, to provide
	 */
	private Server localServer;

	/**
	 * remote station object
	 */
	private IStation station;

	/**
	 * remote browser object
	 */
	private IBrowser browser;

	/**
	 * current selected remote player object
	 */
	private IPlayer player;

	/**
	 * remote control object
	 */
	private IControl control;

	/**
	 * listener for player
	 */
	private PlayerListener playerListener;

	/**
	 * remote playlist object
	 */
	private IPlayList playList;

	/**
	 * remote chatserver object
	 */
	private IChatServer chatServer;

	/**
	 * handler to post actions in the ui thread
	 */
	private Handler handler = new Handler();

	/**
	 * database object to the local database
	 */
	private ServerDatabase serverDB;

	@Override
	public void onCreate() {
		super.onCreate();
		binder = new PlayerBinder();
		playerListener = new PlayerListener();
		serverDB = new ServerDatabase(this);
	}

	/**
	 * create connection, execute runnable if connection has started in the ui
	 * thread.
	 * 
	 * @param successRunnable
	 */
	private void connect(final Runnable successRunnable) {
		new Thread() {

			@Override
			public void run() {
				localServer = Server.getServer();
				try {
					try {
						localServer.connectToRegistry(serverIP);
					} catch (SocketException e) {
						localServer.connectToRegistry(serverIP);
					}
					localServer.startServer();

					station = (IStation) localServer.find(
							ControlConstants.STATION_ID, IStation.class);

					if (station == null)
						throw new RemoteException(ControlConstants.STATION_ID,
								"station not found in registry");

					browser = new BufferBrowser(station.createBrowser());
					player = station.getMPlayer();
					player.addPlayerMessageListener(playerListener);
					control = station.getControl();
					playList = station.getPlayList();
					chatServer = station.getChatServer();
					if (successRunnable != null)
						handler.post(successRunnable);
				} catch (final Exception e) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(RemoteService.this, e.getMessage(),
									Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}.start();
	}

	/**
	 * create notification
	 * 
	 * @param title
	 * @param body
	 */
	private void makeNotification(String title, String body) {
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.browser;
		Notification notification = new Notification(icon, "Player started",
				System.currentTimeMillis());
		Intent nIntent = new Intent(this, BrowserActivity.class);
		nIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		nIntent.putExtra(BrowserActivity.EXTRA_SERVER_NAME, serverName);
		PendingIntent pInent = PendingIntent.getActivity(this, 0, nIntent, 0);
		notification.setLatestEventInfo(getApplicationContext(), title, body,
				pInent);
		nm.notify(NOTIFICATION_ID, notification);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(RemoteService.NOTIFICATION_ID);
		disconnect();
		serverDB.close();
	}

	/**
	 * disconnect from current cunnection
	 */
	private void disconnect() {
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
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	/**
	 * listener for player activity. make notification if any message comes.
	 * 
	 * @author sebastian
	 */
	public class PlayerListener implements IPlayerListener {

		@Override
		public void playerMessage(final PlayingBean playing) {
			StringBuilder sb = new StringBuilder();
			String t = "Playing";
			if (playing.getTitle() != null && playing.getTitle().length() > 0)
				t = playing.getTitle();
			if (playing.getArtist() != null && playing.getArtist().length() > 0)
				sb.append("Artist: " + playing.getArtist() + "\n");
			if (playing.getAlbum() != null && playing.getAlbum().length() > 0)
				sb.append("Album: " + playing.getAlbum() + "\n");
			if (playing.getState() == STATE.DOWN)
				t = "player is down";
			final String msg = sb.toString();
			final String title = t;
			handler.post(new Runnable() {
				@Override
				public void run() {
					if (playing.getState() == STATE.DOWN) {
						NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
						nm.cancel(RemoteService.NOTIFICATION_ID);
					} else
						makeNotification(title, msg);
				}
			});
		}
	}

	/**
	 * binder as api for this service. it provides all functionality to the
	 * remote server.
	 * 
	 * @author sebastian
	 */
	public class PlayerBinder extends Binder {

		/**
		 * get remote browser object
		 * 
		 * @return browser
		 */
		public IBrowser getBrowser() {
			return browser;
		}

		/**
		 * get current remote player object
		 * 
		 * @return player
		 */
		public IPlayer getPlayer() {
			return player;
		}

		/**
		 * get remote control object
		 * 
		 * @return control
		 */
		public IControl getControl() {
			return control;
		}

		/**
		 * connect to server, ip of the server will be load from the database
		 * 
		 * @param name
		 * @param r
		 */
		public void connectToServer(String name, Runnable r) {
			if (name.equals(serverName)) {
				if (r != null)
					r.run();
			} else {
				disconnect();
				serverName = name;
				serverIP = serverDB.getIpOfServer(name);
				connect(r);
			}
		}

		/**
		 * set mplayer for current player
		 * 
		 * @throws RemoteException
		 */
		public void useMPlayer() throws RemoteException {
			player = station.getMPlayer();
		}

		/**
		 * set totem for current player
		 * 
		 * @throws RemoteException
		 */
		public void useTotemPlayer() throws RemoteException {
			player = station.getTotemPlayer();
		}

		/**
		 * get remote playlist object
		 * 
		 * @return playlist
		 */
		public IPlayList getPlayList() {
			return playList;
		}

		/**
		 * get remote chatserver object
		 * 
		 * @return chatserver
		 */
		public IChatServer getChatServer() {
			return chatServer;
		}

		/**
		 * @return true if there is a connection wich a server
		 */
		public boolean isConnected() {
			return station != null;
		}

		/**
		 * @return name of connected server
		 */
		public String getServerName() {
			return serverName;
		}
	}

}
