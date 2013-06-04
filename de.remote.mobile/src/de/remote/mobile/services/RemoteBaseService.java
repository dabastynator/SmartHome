package de.remote.mobile.services;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.ControlConstants;
import de.remote.api.IBrowser;
import de.remote.api.IChatServer;
import de.remote.api.IControl;
import de.remote.api.IMusicStation;
import de.remote.api.IPlayList;
import de.remote.api.IPlayer;
import de.remote.api.IStationHandler;
import de.remote.api.PlayerException;
import de.remote.api.PlayingBean;
import de.remote.gpiopower.api.IGPIOPower;
import de.remote.mobile.R;
import de.remote.mobile.activities.BrowserActivity;
import de.remote.mobile.database.ServerDatabase;
import de.remote.mobile.services.RemoteService.IRemoteActionListener;
import de.remote.mobile.services.RemoteService.MobileReceiverListener;
import de.remote.mobile.services.RemoteService.PlayerListener;

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
	protected static final int DOWNLOAD_PORT = 5021;

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
	protected IMusicStation station;
	
	/**
	 * remote station list object
	 */
	protected IStationHandler stationList;	
	
	/**
	 * list of available music stations
	 */
	protected Map<String, IMusicStation> musicStations = new HashMap<String, IMusicStation>();

	protected Map<IMusicStation, StationStuff> stationStuff = new HashMap<IMusicStation, RemoteBaseService.StationStuff>();
	
	/**
	 * remote browser object
	 */
	protected IBrowser browser;

	/**
	 * gpio power point
	 */
	protected IGPIOPower power;

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
	 * remote chat server object
	 */
	public IChatServer chatServer;

	/**
	 * create connection, execute runnable if connection has started in the ui
	 * thread.
	 * 
	 * @param successRunnable
	 */
	protected void connect() {
		localServer = Server.getServer();
		try {
			try {
				localServer.connectToRegistry(serverIP);
			} catch (SocketException e) {
				localServer.connectToRegistry(serverIP);
			}
			localServer.startServer();

			stationList = (IStationHandler) localServer.find(IStationHandler.STATION_ID, IStationHandler.class);
			if (stationList == null)
				throw new RemoteException(IStationHandler.STATION_ID,
						"music handler not found in registry");	
			
			int stationSize = stationList.getStationSize();
			musicStations.clear();
			stationStuff.clear();
			for (int i=0; i<stationSize; i++){
				IMusicStation musicStation = stationList.getStation(i);
				String name = musicStation.getName();
				musicStations.put(name, musicStation);
			}

			chatServer = (IChatServer) localServer.find(
					ControlConstants.CHAT_ID, IChatServer.class);
			power = (IGPIOPower) localServer.find(IGPIOPower.ID,
					IGPIOPower.class);
			handler.post(new Runnable() {
				@Override
				public void run() {
					for (IRemoteActionListener listener : actionListener)
						listener.onServerConnectionChanged(serverName);
				}
			});
		} catch (final Exception e) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(RemoteBaseService.this, e.getMessage(),
							Toast.LENGTH_SHORT).show();
					for (IRemoteActionListener listener : actionListener)
						listener.onServerConnectionChanged(null);
				}
			});
		}
	}

	public void registerAndUpdate() throws RemoteException, PlayerException {
		Log.e("Remote Control", "register and update");
		if (station == null)
			return;
		station.getMPlayer().addPlayerMessageListener(playerListener);
		station.getTotemPlayer().addPlayerMessageListener(playerListener);
		PlayingBean bean = player.getPlayingBean();
		playerListener.playerMessage(bean);
	}

	/**
	 * create notification about downloading file
	 * 
	 * @param title
	 * @param body
	 */
	protected void makeDonwloadingNotification(String file, float progress) {
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.download,
				"Download started", System.currentTimeMillis());
		notification.contentView = new RemoteViews(getPackageName(),
				R.layout.download_progress);
		notification.contentView.setImageViewResource(R.id.status_icon,
				R.drawable.download);
		notification.contentView.setTextViewText(R.id.status_text, "download "
				+ file);
		notification.contentView.setProgressBar(R.id.status_progress, 100,
				(int) (progress * 100), false);
		Intent nIntent = new Intent(this, BrowserActivity.class);
		nIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		nIntent.putExtra(BrowserActivity.EXTRA_SERVER_ID, serverID);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, nIntent, 0);
		notification.contentIntent = pIntent;
		nm.notify(DOWNLOAD_NOTIFICATION_ID, notification);
	}

	@Override
	public void onDestroy() {
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(RemoteService.PLAYING_NOTIFICATION_ID);
		nm.cancel(RemoteService.DOWNLOAD_NOTIFICATION_ID);
		for (IRemoteActionListener listener : actionListener)
			listener.onStopService();
		disconnect();
		serverDB.close();
		super.onDestroy();
	}

	/**
	 * disconnect from current connection
	 */
	protected void disconnect() {
		if (player != null) {
			try {
				station.getMPlayer()
						.removePlayerMessageListener(playerListener);
				station.getTotemPlayer().removePlayerMessageListener(
						playerListener);
			} catch (Exception e) {
			}
		}
		if (localServer != null)
			localServer.close();
		station = null;
		musicStations.clear();
		stationList = null;
		stationStuff.clear();
		browser = null;
		player = null;
		serverID = -1;
		serverName = null;
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(RemoteService.PLAYING_NOTIFICATION_ID);
//		handler.post(new Runnable() {
//			public void run() {
//				if (actionListener == null)
//					actionListener = new ArrayList<IRemoteActionListener>();
//				for (IRemoteActionListener listener : actionListener)
//					listener.OnServerConnectionChanged(null);
//			}
//		});
	}

	@Override
	public PlayerBinder onBind(Intent intent) {
		return binder;
	}

	/**
	 * @return local server
	 */
	public Server getServer() {
		return localServer;
	}
	
	public static class StationStuff{
		public IBrowser browser;
		public IPlayer player;
		public IPlayList pls;
		public IControl control;
	}

}
