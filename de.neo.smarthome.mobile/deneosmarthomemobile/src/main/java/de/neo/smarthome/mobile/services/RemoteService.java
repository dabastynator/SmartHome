package de.neo.smarthome.mobile.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import de.neo.android.persistence.Dao;
import de.neo.android.persistence.DaoException;
import de.neo.android.persistence.DaoFactory;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebProxyBuilder;
import de.neo.smarthome.mobile.activities.MediaServerActivity;
import de.neo.smarthome.mobile.api.IControlCenter;
import de.neo.smarthome.mobile.api.IWebMediaServer;
import de.neo.smarthome.mobile.api.IWebMediaServer.BeanMediaServer;
import de.neo.smarthome.mobile.api.IWebSwitch;
import de.neo.smarthome.mobile.api.IWebSwitch.BeanSwitch;
import de.neo.smarthome.mobile.api.IWebSwitch.State;
import de.neo.smarthome.mobile.api.PlayerException;
import de.neo.smarthome.mobile.activities.ControlSceneActivity;
import de.neo.smarthome.mobile.activities.SettingsActivity;
import de.neo.smarthome.mobile.persistence.RemoteDaoBuilder;
import de.neo.smarthome.mobile.persistence.RemoteServer;
import de.neo.smarthome.mobile.receivers.MusicWidgetProvider;
import de.neo.smarthome.mobile.receivers.SwitchWidgetProvider;
import de.neo.smarthome.mobile.tasks.DownloadQueue;
import de.neo.smarthome.mobile.tasks.WidgetUpdateTask;
import de.neo.smarthome.mobile.tasks.WifiSignalTask;
import de.neo.smarthome.mobile.util.ExceptionHandler;
import de.neo.smarthome.mobile.util.WidgetUpdater;
import de.remote.mobile.R;

/**
 * this service updates a widget and handles actions on the widget.
 * 
 * @author sebastian
 */
public class RemoteService extends Service {

	public static final String ACTION_SWITCH = "de.remote.power.SWITCH";
	public static final String ACTION_UPDATE = "de.remote.power.UPDATE";
	public static final String ACTION_PLAY = "de.remote.mobile.ACTION_PLAY";
	public static final String ACTION_STOP = "de.remote.mobile.ACTION_STOP";
	public static final String ACTION_NEXT = "de.remote.mobile.ACTION_NEXT";
	public static final String ACTION_PREV = "de.remote.mobile.ACTION_PREV";
	public static final String ACTION_VOLUP = "de.remote.mobile.ACTION_VOL_UP";
	public static final String ACTION_VOLDOWN = "de.remote.mobile.ACTION_VOL_DOWN";
	public static final String ACTION_VOLUME = "de.remote.mobile.ACTION_VOLUME";
	public static final String ACTION_DOWNLOAD = "de.remote.mobile.DOWNLOAD";
	public static final String ACTION_FOREGROUND = "de.remote.mobile.FOREGROUND";
	public static final String ACTION_MEDIA_ACTIVITY = "de.remote.mobile.MEDIA_ACTIVITY";

	public static final String EXTRA_WIDGET = "widget_id";
	public static final String EXTRA_DOWNLOAD = "download_file";
	public static final String EXTRA_DOWNLOAD_DESTINY = "download_destiny";
	public static final String EXTRA_ID = "remote_id";
	public static final String PREFERENCES = "preferences.widget";

	public static final int NOTIFICATION_ID = 1;

	public static boolean DEBUGGING = false;

	private Handler mHandler = new Handler();
	private IWebSwitch mWebSwitch;
	private IWebMediaServer mWebMediaServer;
	private DownloadQueue mDownloader;
	private WidgetUpdater mWidgedUpdater;
	private WifiSignalTask mWifiSignalTask;
	private RemoteServer mFavorite;
	private WidgetUpdateTask mWidgetUpdateTask;
	private ExceptionHandler mHandleAppCrash;
	private DaoFactory mDaoFactory;
	private IControlCenter mWebControlCenter;

	@Override
	public void onCreate() {
		mHandleAppCrash = new ExceptionHandler(this);
		Thread.setDefaultUncaughtExceptionHandler(mHandleAppCrash);
		super.onCreate();
		DaoFactory.initiate(new RemoteDaoBuilder(this));
		mDaoFactory = DaoFactory.getInstance();
		mDownloader = new DownloadQueue(getApplicationContext(), mHandler);
		mDownloader.start();
		mWidgedUpdater = new WidgetUpdater(getApplicationContext());
		mWifiSignalTask = new WifiSignalTask(this);
		mWifiSignalTask.start();
		mWidgetUpdateTask = new WidgetUpdateTask(getApplicationContext());
		mWidgetUpdateTask.start();
		refreshWebApi();
		updateMusicWidget();
		updateSwitchWidget();
		updateForegroundNotification();
	};

	@Override
	public void onDestroy() {
		mDownloader.setRunning(false);
		mWifiSignalTask.setRunning(false);
		mWidgetUpdateTask.setRunning(false);
		super.onDestroy();
	}

	private void refreshWebApi() {
		try {
			Dao<RemoteServer> dao = mDaoFactory.getDao(RemoteServer.class);
			List<RemoteServer> serverList = dao.loadAll();
			mFavorite = null;
			for (RemoteServer server : serverList) {
				if (server.isFavorite())
					mFavorite = server;
			}
			if (mFavorite != null) {
				WebProxyBuilder b = new WebProxyBuilder().setSecurityToken(mFavorite.getApiToken());
				String url = mFavorite.getEndPoint();
				mWebSwitch = b.setEndPoint(url + "/switch").setInterface(IWebSwitch.class).create();
				mWebMediaServer = b.setEndPoint(url + "/mediaserver").setInterface(IWebMediaServer.class).create();
				mWebControlCenter = b.setEndPoint(url + "/controlcenter").setInterface(IControlCenter.class).create();
			}
		} catch (DaoException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		refreshWebApi();
		if (intent != null && intent.getAction() != null) {
			if (ACTION_UPDATE.equals(intent.getAction())) {
				updateMusicWidget();
				updateSwitchWidget();
			} else if (ACTION_DOWNLOAD.equals(intent.getAction())) {
				if (intent.hasExtra(EXTRA_DOWNLOAD) && intent.hasExtra(EXTRA_ID)) {
					Object download = intent.getExtras().get(EXTRA_DOWNLOAD);
					String id = intent.getStringExtra(EXTRA_ID);
					String destiny = intent.getStringExtra(EXTRA_DOWNLOAD_DESTINY);
					mDownloader.download(mWebMediaServer, id, destiny, download);
				}
			} else if (ACTION_FOREGROUND.equals(intent.getAction())) {
				updateForegroundNotification();
			} else if (ACTION_MEDIA_ACTIVITY.equals(intent.getAction())){
				int widgetID = intent.getIntExtra(EXTRA_WIDGET, 0);
				SharedPreferences prefs = getSharedPreferences(PREFERENCES, 0);
				final String remoteID = prefs.getString(widgetID + "", null);
				if (remoteID != null){
					Intent i = new Intent(this, MediaServerActivity.class);
					i.putExtra(MediaServerActivity.EXTRA_MEDIA_ID, remoteID);
					startActivity(i);
				}
			} else {
				new Thread() {
					public void run() {
						executeRemoteCommand(intent.getAction(), intent);
					}
				}.start();
			}
		} else {
			updateMusicWidget();
			updateSwitchWidget();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void updateForegroundNotification() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean foreground = preferences.getBoolean(SettingsActivity.FOREGROUND, true);
		if (foreground) {
			Intent nIntent = new Intent(getApplicationContext(), ControlSceneActivity.class);
			nIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			PendingIntent pInent = PendingIntent.getActivity(getApplicationContext(), 0, nIntent, 0);

			NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
			builder.setContentText(getApplicationName());
			if (mFavorite != null)
				builder.setContentTitle(mFavorite.getName());
			builder.setSmallIcon(R.drawable.smarthome_notify);
			builder.setContentIntent(pInent);

			startForeground(NOTIFICATION_ID, builder.build());
		} else {
			stopForeground(true);
		}
	}

	public String getApplicationName() {
		ApplicationInfo applicationInfo = getApplicationContext().getApplicationInfo();
		int stringId = applicationInfo.labelRes;
		return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : getString(stringId);
	}

	private void executeRemoteCommand(final String action, final Intent intent) {
		try {
			int widgetID = intent.getIntExtra(EXTRA_WIDGET, 0);
			SharedPreferences prefs = getSharedPreferences(PREFERENCES, 0);
			final String remoteID = prefs.getString(widgetID + "", null);
			if (remoteID == null)
				return;
			if (action.equals(ACTION_SWITCH)) {
				switchPower(remoteID, widgetID);
			} else if (action.equals(ACTION_PLAY) || action.equals(ACTION_STOP) || action.equals(ACTION_NEXT)
					|| action.equals(ACTION_PREV) || action.equals(ACTION_VOLDOWN) || action.equals(ACTION_VOLUP)) {
				musicAction(remoteID, widgetID, action);
			}

		} catch (final Exception e) {
			e.printStackTrace();
			mHandler.post(new Runnable() {
				public void run() {
					Toast.makeText(RemoteService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			});
		}

	}

	protected void musicAction(String remoteID, int widgetID, String action) throws RemoteException, PlayerException {
		String player = "mplayer";
		ArrayList<BeanMediaServer> list = null;
		switch (action) {
		case ACTION_PLAY:
			mWebMediaServer.playPause(remoteID, player);
			break;
		case ACTION_STOP:
			mWebMediaServer.playStop(remoteID, player);
			break;
		case ACTION_NEXT:
			mWebMediaServer.playNext(remoteID, player);
			break;
		case ACTION_PREV:
			mWebMediaServer.playPrevious(remoteID, player);
			break;
		case ACTION_VOLDOWN:
			list = mWebMediaServer.getMediaServer(remoteID);
			if (list.size() > 0 && list.get(0).getCurrentPlaying() != null) {
				int volume = list.get(0).getCurrentPlaying().getVolume();
				mWebMediaServer.setVolume(remoteID, player, Math.max(0, volume - 5));
			}
			break;
		case ACTION_VOLUP:
			list = mWebMediaServer.getMediaServer(remoteID);
			if (list.size() > 0 && list.get(0).getCurrentPlaying() != null) {
				int volume = list.get(0).getCurrentPlaying().getVolume();
				mWebMediaServer.setVolume(remoteID, player, Math.min(100, volume + 5));
			}
		}
		list = mWebMediaServer.getMediaServer(remoteID);
		if (list.size() == 1) {
			mWidgedUpdater.updateMusicWidget(widgetID, list.get(0));
		}
	}

	private void updateMusicWidget() {

		// update each of the app widgets with the remote adapter
		Thread thread = new Thread() {
			public void run() {
				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
				ComponentName thisWidget = new ComponentName(getApplicationContext(), MusicWidgetProvider.class);
				int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
				SharedPreferences prefs = getSharedPreferences(PREFERENCES, 0);
				try {

					ArrayList<BeanMediaServer> server = mWebMediaServer.getMediaServer("");
					Map<String, BeanMediaServer> serverMap = new HashMap<>();
					for (BeanMediaServer s : server)
						serverMap.put(s.getID(), s);
					for (int widgetID : appWidgetIds) {
						String serverID = prefs.getString(widgetID + "", null);
						BeanMediaServer s = serverMap.get(serverID);
						if (s != null)
							mWidgedUpdater.updateMusicWidget(widgetID, s);
					}
					mWifiSignalTask.setConnection(true);
				} catch (RemoteException e) {
					for (int widgetID : appWidgetIds) {
						mWidgedUpdater.updateMusicWidget(widgetID, e);
					}
				}
			};
		};
		if (mWebMediaServer != null)
			thread.start();
	}

	private void updateSwitchWidget() {

		// update each of the app widgets with the remote adapter
		Thread thread = new Thread() {
			public void run() {
				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
				ComponentName thisWidget = new ComponentName(getApplicationContext(), SwitchWidgetProvider.class);
				int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
				try {
					SharedPreferences prefs = getSharedPreferences(PREFERENCES, 0);
					ArrayList<BeanSwitch> switches = mWebSwitch.getSwitches();
					Map<String, BeanSwitch> switchMap = new HashMap<>();
					for (BeanSwitch s : switches)
						switchMap.put(s.getID(), s);
					for (int widgetID : appWidgetIds) {
						String switchID = prefs.getString(widgetID + "", null);
						BeanSwitch s = switchMap.get(switchID);
						if (s != null)
							mWidgedUpdater.updateSwitchWidget(widgetID, s);
					}
				} catch (Exception e) {
					for (int widgetID : appWidgetIds) {
						mWidgedUpdater.updateSwitchWidget(widgetID, e);
					}
				}
			};
		};
		if (mWebSwitch != null)
			thread.start();
	}

	private void switchPower(String switchID, int widgetID) throws Exception {

		for (BeanSwitch bSwitch : mWebSwitch.getSwitches()) {
			if (bSwitch.getID().equals(switchID)) {
				String newState = (bSwitch.getState() == State.ON ? "OFF" : "ON");
				BeanSwitch newSwitch = mWebSwitch.setSwitchState(bSwitch.getID(), newState);
				mWidgedUpdater.updateSwitchWidget(widgetID, newSwitch);
			}
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public IControlCenter getWebControlCenter() {
		return mWebControlCenter;
	}
}
