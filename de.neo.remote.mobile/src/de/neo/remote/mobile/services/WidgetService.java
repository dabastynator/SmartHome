package de.neo.remote.mobile.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;
import de.neo.android.persistence.Dao;
import de.neo.android.persistence.DaoException;
import de.neo.android.persistence.DaoFactory;
import de.neo.remote.api.IWebMediaServer;
import de.neo.remote.api.IWebMediaServer.BeanMediaServer;
import de.neo.remote.api.IWebSwitch;
import de.neo.remote.api.IWebSwitch.BeanSwitch;
import de.neo.remote.api.IWebSwitch.State;
import de.neo.remote.api.PlayerException;
import de.neo.remote.api.PlayingBean.STATE;
import de.neo.remote.mobile.activities.MediaServerActivity;
import de.neo.remote.mobile.persistence.RemoteDaoBuilder;
import de.neo.remote.mobile.persistence.RemoteServer;
import de.neo.remote.mobile.receivers.MusicWidgetProvider;
import de.neo.remote.mobile.receivers.SwitchWidgetProvider;
import de.neo.remote.mobile.tasks.DownloadQueue;
import de.neo.remote.mobile.util.ControlSceneRenderer;
import de.neo.rmi.api.WebProxyBuilder;
import de.neo.rmi.protokol.RemoteException;
import de.remote.mobile.R;

/**
 * this service updates a widget and handles actions on the widget.
 * 
 * @author sebastian
 */
public class WidgetService extends Service {

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

	public static final String EXTRA_WIDGET = "widget_id";
	public static final String EXTRA_DOWNLOAD = "download_file";
	public static final String EXTRA_ID = "remote_id";
	public static final String PREFERENCES = "preferences.widget";

	public static boolean DEBUGGING = false;

	private Handler mHandler = new Handler();
	private IWebSwitch mWebSwitch;
	private IWebMediaServer mWebMediaServer;
	private DownloadQueue mDownloader;

	@Override
	public void onCreate() {
		super.onCreate();
		mDownloader = new DownloadQueue(getApplicationContext());
		refreshWebApi();
		updateMusicWidget();
		updateSwitchWidget();
	};

	@Override
	public void onDestroy() {
		mDownloader.setRunning(false);
		super.onDestroy();
	}

	private void refreshWebApi() {
		DaoFactory.initiate(new RemoteDaoBuilder(this));
		try {
			Dao<RemoteServer> dao = DaoFactory.getInstance().getDao(RemoteServer.class);
			List<RemoteServer> serverList = dao.loadAll();
			RemoteServer favorite = null;
			for (RemoteServer server : serverList) {
				if (server.isFavorite())
					favorite = server;
			}
			if (favorite != null) {
				mWebSwitch = new WebProxyBuilder().setEndPoint(favorite.getEndPoint() + "/switch")
						.setSecurityToken(favorite.getApiToken()).setInterface(IWebSwitch.class).create();
				mWebMediaServer = new WebProxyBuilder().setEndPoint(favorite.getEndPoint() + "/mediaserver")
						.setSecurityToken(favorite.getApiToken()).setInterface(IWebMediaServer.class).create();
			}
		} catch (DaoException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		refreshWebApi();
		if (intent != null && intent.getAction() != null) {
			if (ACTION_UPDATE.equals(intent.getAction())) {
				updateMusicWidget();
				updateSwitchWidget();
			} else if (ACTION_DOWNLOAD.equals(intent.getAction())) {
				if (intent.hasExtra(EXTRA_DOWNLOAD) && intent.hasExtra(EXTRA_ID)) {
					String file = intent.getStringExtra(EXTRA_DOWNLOAD);
					String id = intent.getStringExtra(EXTRA_ID);
					mDownloader.download(mWebMediaServer, id, file);
				}
			} else
				executeRemoteCommand(intent.getAction(), intent);
		} else {
			updateMusicWidget();
			updateSwitchWidget();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * execute given action
	 * 
	 * @param action
	 * @return true if action is known
	 */
	private void executeRemoteCommand(final String action, final Intent intent) {
		new Thread() {
			public void run() {
				try {
					int widgetID = intent.getIntExtra(EXTRA_WIDGET, 0);
					SharedPreferences prefs = getSharedPreferences(PREFERENCES, 0);
					final String remoteID = prefs.getString(widgetID + "", null);
					if (remoteID == null)
						return;
					if (action.equals(ACTION_SWITCH)) {
						switchPower(remoteID, widgetID);
					} else if (action.equals(ACTION_PLAY) || action.equals(ACTION_STOP) || action.equals(ACTION_NEXT)
							|| action.equals(ACTION_PREV) || action.equals(ACTION_VOLDOWN)
							|| action.equals(ACTION_VOLUP)) {
						musicAction(remoteID, widgetID, action);
					}

				} catch (final Exception e) {
					e.printStackTrace();
					mHandler.post(new Runnable() {
						public void run() {
							Toast.makeText(WidgetService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}.start();
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
			updateMusicWidget(getApplicationContext(), widgetID, list.get(0));
		}
	}

	public static void updateMusicWidget(Context context, int widgetID, BeanMediaServer mediaserver) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.mediaserver_widget);
		if (remoteViews != null) {
			remoteViews.setViewVisibility(R.id.img_widget_thumbnail, View.INVISIBLE);
			if (mediaserver == null) {
				remoteViews.setTextViewText(R.id.lbl_widget_big, context.getString(R.string.no_conneciton));
				remoteViews.setTextViewText(R.id.lbl_widget_small,
						context.getString(R.string.no_conneciton_with_server));
				remoteViews.setTextViewText(R.id.lbl_widget_small2, "");
				remoteViews.setInt(R.id.button_widget_play, "setBackgroundResource", R.drawable.player_pause);
			} else if (mediaserver.getCurrentPlaying() != null) {
				remoteViews.setTextViewText(R.id.lbl_widget_big, mediaserver.getCurrentPlaying().getTitle());
				remoteViews.setTextViewText(R.id.lbl_widget_small, mediaserver.getCurrentPlaying().getArtist());
				remoteViews.setTextViewText(R.id.lbl_widget_small2, mediaserver.getCurrentPlaying().getAlbum());
				Intent browserIntent = new Intent(context, MediaServerActivity.class);
				browserIntent.putExtra(MediaServerActivity.EXTRA_MEDIA_ID, mediaserver.getID());
				PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, browserIntent, 0);

				remoteViews.setOnClickPendingIntent(R.id.lbl_widget_big, pendingIntent);

				if (mediaserver.getCurrentPlaying().getState() == STATE.PLAY)
					remoteViews.setInt(R.id.button_widget_play, "setBackgroundResource", R.drawable.player_pause);
				else
					remoteViews.setInt(R.id.button_widget_play, "setBackgroundResource", R.drawable.player_play);
			} else {
				remoteViews.setTextViewText(R.id.lbl_widget_big, context.getString(R.string.player_no_file_playing));
				remoteViews.setTextViewText(R.id.lbl_widget_small, mediaserver.getName());
				remoteViews.setTextViewText(R.id.lbl_widget_small2, "");
				remoteViews.setInt(R.id.button_widget_play, "setBackgroundResource", R.drawable.player_pause);
			}

			Intent browserIntent = new Intent(context, MediaServerActivity.class);
			browserIntent.putExtra(MediaServerActivity.EXTRA_MEDIA_ID, mediaserver.getID());
			browserIntent.setData(Uri.withAppendedPath(Uri.parse("ABCD://widget/id/"), String.valueOf(widgetID)));
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, browserIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.lbl_widget_big, pendingIntent);

			// set play functionality
			Intent playIntent = new Intent(context, WidgetService.class);
			playIntent.setAction(ACTION_PLAY);
			playIntent.setData(Uri.withAppendedPath(Uri.parse("ABCD://widget/id/"), String.valueOf(widgetID)));
			playIntent.putExtra(EXTRA_WIDGET, widgetID);
			PendingIntent playPending = PendingIntent.getService(context, 0, playIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.button_widget_play, playPending);

			// set stop functionality
			Intent stopIntent = new Intent(context, WidgetService.class);
			stopIntent.setAction(ACTION_STOP);
			stopIntent.setData(Uri.withAppendedPath(Uri.parse("ABCD://widget/id/"), String.valueOf(widgetID)));
			stopIntent.putExtra(EXTRA_WIDGET, widgetID);
			PendingIntent stopPending = PendingIntent.getService(context, 0, stopIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.button_widget_quit, stopPending);

			// set vol up functionality
			Intent nextIntent = new Intent(context, WidgetService.class);
			nextIntent.setAction(ACTION_VOLUP);
			nextIntent.setData(Uri.withAppendedPath(Uri.parse("ABCD://widget/id/"), String.valueOf(widgetID)));
			nextIntent.putExtra(EXTRA_WIDGET, widgetID);
			PendingIntent nextPending = PendingIntent.getService(context, 0, nextIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.button_widget_vol_up, nextPending);

			// set vol down functionality
			Intent prevIntent = new Intent(context, WidgetService.class);
			prevIntent.setAction(ACTION_VOLDOWN);
			prevIntent.setData(Uri.withAppendedPath(Uri.parse("ABCD://widget/id/"), String.valueOf(widgetID)));
			prevIntent.putExtra(EXTRA_WIDGET, widgetID);
			PendingIntent prevPending = PendingIntent.getService(context, 0, prevIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.button_widget_vol_down, prevPending);

			appWidgetManager.updateAppWidget(widgetID, remoteViews);
			if (DEBUGGING)
				Toast.makeText(context, mediaserver.getCurrentPlaying().getArtist(), Toast.LENGTH_SHORT).show();
		}
	}

	private void updateMusicWidget() {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());

		ComponentName thisWidget = new ComponentName(getApplicationContext(), MusicWidgetProvider.class);
		final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		// update each of the app widgets with the remote adapter
		Thread thread = new Thread() {
			public void run() {
				try {
					SharedPreferences prefs = getSharedPreferences(PREFERENCES, 0);
					ArrayList<BeanMediaServer> server = mWebMediaServer.getMediaServer("");
					Map<String, BeanMediaServer> serverMap = new HashMap<>();
					for (BeanMediaServer s : server)
						serverMap.put(s.getID(), s);
					for (int widgetID : appWidgetIds) {
						String serverID = prefs.getString(widgetID + "", null);
						BeanMediaServer s = serverMap.get(serverID);
						if (s != null)
							updateMusicWidget(getApplicationContext(), widgetID, s);
					}
				} catch (Exception e) {
					System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
			};
		};
		thread.start();
	}

	private void updateSwitchWidget() {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());

		ComponentName thisWidget = new ComponentName(getApplicationContext(), SwitchWidgetProvider.class);
		final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		// update each of the app widgets with the remote adapter
		Thread thread = new Thread() {
			public void run() {
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
							updateSwitchWidget(getApplicationContext(), widgetID, s);
					}
				} catch (Exception e) {
					System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
			};
		};
		thread.start();
	}

	public static void updateSwitchWidget(Context context, int widgetID, BeanSwitch s) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		RemoteViews remoteSwitchViews = new RemoteViews(context.getPackageName(), R.layout.switch_widget);
		remoteSwitchViews.setImageViewResource(R.id.image_power_widget,
				getImageForSwitchType(s.getType(), s.getState() == State.ON));
		remoteSwitchViews.setTextViewText(R.id.text_power_widget, s.getName());

		// set switch functionality
		Intent switchIntent = new Intent(context, WidgetService.class);
		switchIntent.setAction(ACTION_SWITCH);
		switchIntent.setData(Uri.withAppendedPath(Uri.parse("ABCD://widget/id/"), String.valueOf(widgetID)));
		switchIntent.putExtra(EXTRA_WIDGET, widgetID);
		PendingIntent switchPending = PendingIntent.getService(context, widgetID, switchIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		remoteSwitchViews.setOnClickPendingIntent(R.id.widget_power_layout, switchPending);

		appWidgetManager.updateAppWidget(widgetID, remoteSwitchViews);
	}

	private void switchPower(String switchID, int widgetID) throws Exception {

		for (BeanSwitch bSwitch : mWebSwitch.getSwitches()) {
			if (bSwitch.getID().equals(switchID)) {
				String newState = (bSwitch.getState() == State.ON ? "OFF" : "ON");
				BeanSwitch newSwitch = mWebSwitch.setSwitchState(bSwitch.getID(), newState);
				updateSwitchWidget(getApplicationContext(), widgetID, newSwitch);
			}
		}

	}

	public void onGroundPlotCreated(de.neo.remote.api.GroundPlot plot) {
	};

	public static int getImageForSwitchType(String type, boolean on) {
		if (ControlSceneRenderer.AUDO.equals(type)) {
			if (on)
				return R.drawable.music_on;
			else
				return R.drawable.music_off;
		}
		if (ControlSceneRenderer.VIDEO.equals(type)) {
			if (on)
				return R.drawable.tv_on;
			else
				return R.drawable.tv_off;
		}
		if (ControlSceneRenderer.LAMP_FLOOR.equals(type) || ControlSceneRenderer.LAMP_LAVA.equals(type)) {
			if (on)
				return R.drawable.light_on;
			else
				return R.drawable.light_off;
		}
		if (ControlSceneRenderer.LAMP_READ.equals(type)) {
			if (on)
				return R.drawable.reading_on;
			else
				return R.drawable.reading_off;
		}
		if (ControlSceneRenderer.SWITCH_COFFEE.equals(type)) {
			if (on)
				return R.drawable.coffee_on;
			else
				return R.drawable.coffee_off;
		}
		return R.drawable.switch_unknown;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
