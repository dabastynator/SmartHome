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
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
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
 * service for remotecontrol a server. the binder enables functions to control
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
	private int serverID;

	/**
	 * ip of current server
	 */
	private String serverIP;

	/**
	 * name of current server
	 */
	private String serverName;

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

	/**
	 * list of all listeners for any action on this service
	 */
	private List<IRemoteActionListener> actionListener = new ArrayList<IRemoteActionListener>();

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
					playerListener.playerMessage(player.getPlayingFile());
					control = station.getControl();
					playList = station.getPlayList();
					chatServer = station.getChatServer();
					if (successRunnable != null)
						handler.post(successRunnable);
					handler.post(new Runnable() {
						@Override
						public void run() {
							for (IRemoteActionListener listener: actionListener)
								listener.serverConnectionChanged(serverName);							
						}
					});
				} catch (final Exception e) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(RemoteService.this, e.getMessage(),
									Toast.LENGTH_SHORT).show();
							for (IRemoteActionListener listener: actionListener)
								listener.serverConnectionChanged(null);
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
		nIntent.putExtra(BrowserActivity.EXTRA_SERVER_ID, serverID);
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
		for (IRemoteActionListener listener:actionListener)
			listener.serverConnectionChanged(null);
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
			Log.e("player", "new file");

			if (playing == null)
				return;
			StringBuilder sb = new StringBuilder();
			String t = "Playing";
			if (playing.getTitle() != null && playing.getTitle().length() > 0)
				t = playing.getTitle();
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
						nm.cancel(RemoteService.NOTIFICATION_ID);
					} else
						makeNotification(title, msg);
					for (IRemoteActionListener listener: actionListener)
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
		 * @param id
		 * @param r
		 */
		public void connectToServer(int id, Runnable r) {
			if (id == serverID) {
				if (r != null)
					r.run();
			} else {
				disconnect();
				serverID = id;
				serverIP = serverDB.getIpOfServer(id);
				serverName = serverDB.getNameOfServer(id);
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

		public PlayerListener getPlayerListener() {
			return playerListener;
		}

		/**
		 * add new remote action listener. the listener will be informed about
		 * actions on this service
		 * 
		 * @param listener
		 */
		public void addRemoteActionListener(IRemoteActionListener listener) {
			if (!actionListener.contains(listener))
				actionListener.add(listener);
		}

		/**
		 * remove action listener
		 * 
		 * @param listener
		 */
		public void removeRemoteActionListener(IRemoteActionListener listener) {
			actionListener.remove(listener);
		}
	}

}
