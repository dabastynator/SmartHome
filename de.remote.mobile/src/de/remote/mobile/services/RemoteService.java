package de.remote.mobile.services;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import de.newsystem.rmi.api.RMILogger;
import de.newsystem.rmi.api.RMILogger.LogPriority;
import de.newsystem.rmi.api.RMILogger.RMILogListener;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.protokol.RemoteException;
import de.newsystem.rmi.transceiver.ReceiverProgress;
import de.remote.api.ControlConstants;
import de.remote.api.IBrowser;
import de.remote.api.IChatServer;
import de.remote.api.IControl;
import de.remote.api.IMusicStation;
import de.remote.api.IPlayList;
import de.remote.api.IPlayer;
import de.remote.api.IPlayerListener;
import de.remote.api.IStationHandler;
import de.remote.api.PlayerException;
import de.remote.api.PlayingBean;
import de.remote.gpiopower.api.IGPIOListener;
import de.remote.gpiopower.api.IGPIOPower;
import de.remote.gpiopower.api.IGPIOPower.State;
import de.remote.gpiopower.api.IGPIOPower.Switch;
import de.remote.mobile.database.RemoteDatabase;
import de.remote.mobile.receivers.WLANReceiver;
import de.remote.mobile.util.NotificationHandler;

public class RemoteService extends Service {

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
	protected Map<String, IMusicStation> musicStations;

	protected Map<IMusicStation, StationStuff> stationStuff;

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

	private WLANReceiver wlanReceiver;

	/**
	 * remote control object
	 */
	protected IControl control;

	/**
	 * listener for player
	 */
	protected PlayerListener playerListener;

	/**
	 * listener for download progress and playing files to update notifications
	 */
	protected NotificationHandler notificationHandler;

	protected ProgressListener downloadListener;

	protected IGPIOListener powerListener;

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
	protected Handler handler;

	/**
	 * database object to the local database
	 */
	protected RemoteDatabase serverDB;

	/**
	 * list of all listeners for any action on this service
	 */
	protected List<IRemoteActionListener> actionListener;

	private RMILogListener rmiLogListener;

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

			stationList = (IStationHandler) localServer.find(
					IStationHandler.STATION_ID, IStationHandler.class);
			if (stationList == null)
				throw new RemoteException(IStationHandler.STATION_ID,
						"music handler not found in registry");

			refreshStations();

			chatServer = (IChatServer) localServer.find(
					ControlConstants.CHAT_ID, IChatServer.class);
			power = (IGPIOPower) localServer.find(IGPIOPower.ID,
					IGPIOPower.class);
//			power.registerPowerSwitchListener(powerListener);
			handler.post(new Runnable() {
				@Override
				public void run() {
					for (IRemoteActionListener listener : actionListener)
						listener.onServerConnectionChanged(serverName, serverID);
				}
			});
		} catch (final Exception e) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(RemoteService.this, e.getMessage(),
							Toast.LENGTH_SHORT).show();
					for (IRemoteActionListener listener : actionListener)
						listener.onServerConnectionChanged(null, -1);
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
//		try {
//			power.registerPowerSwitchListener(powerListener);
//		} catch (RemoteException e) {
//
//		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		handler = new Handler();
		rmiLogListener = new RMILogListener() {
			@Override
			public void rmiLog(LogPriority priority, String message, String id,
					long date) {
				Log.e("RMI Logs", message);
			}
		};
		RMILogger.addLogListener(rmiLogListener);
		binder = new PlayerBinder(this);
		musicStations = new HashMap<String, IMusicStation>();
		stationStuff = new HashMap<IMusicStation, StationStuff>();
		actionListener = new ArrayList<IRemoteActionListener>();
		notificationHandler = new NotificationHandler(this);
		playerListener = new PlayerListener();
		powerListener = new GPIOListener();
		downloadListener = new ProgressListener();
		actionListener.add(notificationHandler);
		serverDB = new RemoteDatabase(this);
		wlanReceiver = new WLANReceiver(this);
		registerReceiver(wlanReceiver, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(wlanReceiver);
		disconnect();
		for (IRemoteActionListener listener : actionListener) {
			listener.onServerConnectionChanged(null, -1);
			listener.onStopService();
		}
		serverDB.close();
		RMILogger.removeLogListener(rmiLogListener);
		super.onDestroy();
	}

	/**
	 * disconnect from current connection
	 */
	private void disconnect() {
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
		power = null;
		chatServer = null;
		serverName = null;
		notificationHandler.removeNotification();
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

	public static class StationStuff {
		public IBrowser browser;
		public IPlayer player;
		public IPlayList pls;
		public IControl control;
	}

	public void connectToServer(final int id) {
		new Thread() {
			public void run() {
				if (id != serverID) {
					disconnect();
					serverID = id;
					serverIP = serverDB.getServerDao().getIpOfServer(id);
					serverName = serverDB.getServerDao().getNameOfServer(id);
					connect();
				}
			}
		}.start();
	}

	public void disconnectFromServer() {
		new Thread() {
			public void run() {
				disconnect();
				handler.post(new Runnable() {
					@Override
					public void run() {
						for (IRemoteActionListener listener : actionListener)
							listener.onServerConnectionChanged(serverName,
									serverID);
					}
				});
			}
		}.start();
	}

	public void refreshStations() {
		int stationSize = 0;
		try {
			stationSize = stationList.getStationSize();
		} catch (RemoteException e1) {
		}
		musicStations.clear();
		stationStuff.clear();
		station = null;
		for (int i = 0; i < stationSize; i++) {
			try {
				IMusicStation musicStation = stationList.getStation(i);
				String name = musicStation.getName();
				musicStations.put(name, musicStation);
			} catch (Exception e) {
			}
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

	/**
	 * the gpio listener listenes for remote power switch cange.
	 * 
	 * @author sebastian
	 * 
	 */
	public class GPIOListener implements IGPIOListener {

		@Override
		public void onPowerSwitchChange(final Switch _switch, final State state)
				throws RemoteException {
			Log.e("gpio power", "Switch: " + _switch + " " + state);
			handler.post(new Runnable() {
				@Override
				public void run() {
					for (IRemoteActionListener listener : actionListener)
						listener.onPowerSwitchChange(_switch, state);
				}
			});
		}

	}

	/**
	 * the progresslistener listens for remote download progess.
	 * 
	 * @author sebastian
	 * 
	 */
	public class ProgressListener implements ReceiverProgress {

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
					Toast.makeText(RemoteService.this, "download finished",
							Toast.LENGTH_SHORT).show();
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
					Toast.makeText(RemoteService.this, "download cancled",
							Toast.LENGTH_SHORT).show();
					for (IRemoteActionListener l : actionListener)
						l.downloadCanceled();
				}
			});
		}

	}

	/**
	 * this interface informs listener about any action on the remote service,
	 * such as new connection, new power switch state or new playing file.
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

		/**
		 * call on power switch change.
		 * 
		 * @param _switch
		 * @param state
		 */
		void onPowerSwitchChange(Switch _switch, State state);

	}

}
