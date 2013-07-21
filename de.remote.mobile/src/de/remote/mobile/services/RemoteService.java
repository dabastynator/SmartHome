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
import de.remote.controlcenter.api.IControlCenter;
import de.remote.controlcenter.api.IControlUnit;
import de.remote.gpiopower.api.IInternetSwitch;
import de.remote.gpiopower.api.IInternetSwitch.State;
import de.remote.gpiopower.api.IInternetSwitchListener;
import de.remote.mediaserver.api.IBrowser;
import de.remote.mediaserver.api.IChatServer;
import de.remote.mediaserver.api.IControl;
import de.remote.mediaserver.api.IMediaServer;
import de.remote.mediaserver.api.IPlayList;
import de.remote.mediaserver.api.IPlayer;
import de.remote.mediaserver.api.IPlayerListener;
import de.remote.mediaserver.api.PlayerException;
import de.remote.mediaserver.api.PlayingBean;
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
	protected IMediaServer station;

	/**
	 * remote station list object
	 */
	protected IControlCenter stationList;

	/**
	 * list of available music stations
	 */
	protected Map<String, IMediaServer> musicStations;

	protected Map<IMediaServer, StationStuff> stationStuff;

	/**
	 * remote browser object
	 */
	protected IBrowser browser;

	/**
	 * gpio power point
	 */
	protected Map<String, IInternetSwitch> internetSwitch;

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

	protected IInternetSwitchListener internetSwitchListener;

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

			stationList = (IControlCenter) localServer.find(IControlCenter.ID,
					IControlCenter.class);
			if (stationList == null)
				throw new RemoteException(IControlCenter.ID,
						"control center not found in registry");

			refreshStations();

			chatServer = (IChatServer) localServer.find(IChatServer.ID,
					IChatServer.class);
			// power.registerPowerSwitchListener(powerListener);
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
		// try {
		// power.registerPowerSwitchListener(powerListener);
		// } catch (RemoteException e) {
		//
		// }
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
		musicStations = new HashMap<String, IMediaServer>();
		stationStuff = new HashMap<IMediaServer, StationStuff>();
		internetSwitch = new HashMap<String, IInternetSwitch>();
		actionListener = new ArrayList<IRemoteActionListener>();
		notificationHandler = new NotificationHandler(this);
		playerListener = new PlayerListener();
		internetSwitchListener = new GPIOListener();
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
		internetSwitch.clear();
		stationList = null;
		stationStuff.clear();
		browser = null;
		player = null;
		serverID = -1;
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
			stationSize = stationList.getControlUnitNumber();
		} catch (RemoteException e1) {
		}
		musicStations.clear();
		stationStuff.clear();
		station = null;
		for (int i = 0; i < stationSize; i++) {
			try {
				IControlUnit unit = stationList.getControlUnit(i);
				Object object = unit.getRemoteableControlObject();
				if (object instanceof IMediaServer) {
					IMediaServer server = (IMediaServer) object;
					String name = server.getName();
					musicStations.put(name, server);
				}
				if (object instanceof IInternetSwitch) {
					IInternetSwitch iswitch = (IInternetSwitch) object;
					String name = unit.getName();
					internetSwitch.put(name, iswitch);
				}
			} catch (Exception e) {
				Log.e("error", e.getMessage());
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
	public class GPIOListener implements IInternetSwitchListener {

		@Override
		public void onPowerSwitchChange(final String switchName,
				final State state) throws RemoteException {
			Log.e("gpio power", "Switch: " + switchName + " " + state);
			handler.post(new Runnable() {
				@Override
				public void run() {
					for (IRemoteActionListener listener : actionListener)
						listener.onPowerSwitchChange(switchName, state);
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
		void onPowerSwitchChange(String _switch, State state);

	}

}
