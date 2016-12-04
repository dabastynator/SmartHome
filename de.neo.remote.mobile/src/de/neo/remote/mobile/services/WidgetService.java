package de.neo.remote.mobile.services;

import java.nio.IntBuffer;
import java.util.List;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;
import de.neo.android.persistence.Dao;
import de.neo.android.persistence.DaoException;
import de.neo.android.persistence.DaoFactory;
import de.neo.remote.api.IInternetSwitch;
import de.neo.remote.api.IWebSwitch;
import de.neo.remote.api.IWebSwitch.BeanSwitch;
import de.neo.remote.api.IWebSwitch.State;
import de.neo.remote.api.PlayingBean;
import de.neo.remote.api.PlayingBean.STATE;
import de.neo.remote.mobile.activities.SelectSwitchActivity;
import de.neo.remote.mobile.persistence.RemoteDaoBuilder;
import de.neo.remote.mobile.persistence.RemoteServer;
import de.neo.remote.mobile.receivers.RemotePowerWidgetProvider;
import de.neo.remote.mobile.receivers.RemoteWidgetProvider;
import de.neo.remote.mobile.services.RemoteService.BufferdUnit;
import de.neo.remote.mobile.services.RemoteService.IRemoteActionListener;
import de.neo.remote.mobile.util.ControlSceneRenderer;
import de.neo.rmi.api.WebProxyBuilder;
import de.neo.rmi.protokol.RemoteException;
import de.remote.mobile.R;

/**
 * this service updates a widget and handles actions on the widget.
 * 
 * @author sebastian
 */
public class WidgetService extends Service implements IRemoteActionListener {

	public static boolean DEBUGGING = false;

	private RemoteBinder mBinder;
	private Handler mHandler = new Handler();
	private IWebSwitch mWebSwitch;

	private ServiceConnection playerConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBinder.removeRemoteActionListener(WidgetService.this);
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBinder = (RemoteBinder) service;
			mBinder.addRemoteActionListener(WidgetService.this);
			if (mBinder.isConnected())
				updateMusicWidget(null);
			else
				setMusicWidgetText(getResources().getString(R.string.no_conneciton),
						getResources().getString(R.string.no_conneciton_with_server), "", false, null);
			updateSwitchWidget();

		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		// bind service
		Intent intent = new Intent(this, RemoteService.class);
		startService(intent);
		bindService(intent, playerConnection, Context.BIND_AUTO_CREATE);
		initializeMusicWidgets();
		updateSwitchWidget();
	};

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
			}
		} catch (DaoException e) {
			e.printStackTrace();
		}
	}

	protected void updateMusicWidget(PlayingBean playing) {
		if (mBinder.getLatestMediaServer() == null) {
			if (mBinder.getServer() == null)
				setMusicWidgetText(getString(R.string.no_conneciton), getString(R.string.no_conneciton_with_server), "",
						false, null);
			else
				setMusicWidgetText(getString(R.string.mediaserver_no_server),
						getString(R.string.mediaserver_no_server) + " (" + mBinder.getServer().getName() + ")", "",
						false, null);
			return;
		}
		if (mBinder.getLatestMediaServer().player != null && playing == null)
			playing = mBinder.getPlayingFile();
		if (playing == null || playing.getState() == STATE.DOWN) {
			setMusicWidgetText(getResources().getString(R.string.player_no_file_playing),
					mBinder.getLatestMediaServer().name, "", false, null);
			return;
		}
		String title = "playing";
		if (playing.getTitle() != null) {
			title = playing.getTitle();
			if (title.length() == 0)
				title = playing.getFile();
		} else
			title = playing.getFile();
		String author = "";
		if (playing.getArtist() != null)
			author = playing.getArtist();
		String album = "";
		if (playing.getAlbum() != null)
			album = playing.getAlbum();
		Bitmap thumbnail = null;
		if (playing.getThumbnailWidth() * playing.getThumbnailHeight() > 0 && playing.getThumbnailRGB() != null) {
			thumbnail = Bitmap.createBitmap(playing.getThumbnailWidth(), playing.getThumbnailHeight(),
					Bitmap.Config.RGB_565);
			IntBuffer buf = IntBuffer.wrap(playing.getThumbnailRGB()); // data
																		// is my
																		// array
			thumbnail.copyPixelsFromBuffer(buf);
		}
		setMusicWidgetText(title, author, album, playing.getState() == STATE.PLAY, thumbnail);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null && intent.getAction() != null)
			executeCommand(intent.getAction(), intent);
		else {
			initializeMusicWidgets();
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
	private void executeCommand(final String action, final Intent intent) {
		new Thread() {
			public void run() {
				try {
					if (mBinder == null)
						throw new RemoteException("not binded", "not binded");
					if (action.equals(RemotePowerWidgetProvider.ACTION_SWITCH)) {
						int widgetID = intent.getIntExtra(SelectSwitchActivity.SWITCH_NUMBER, 0);
						switchPower(widgetID);
						return;
					}
					if (mBinder.getLatestMediaServer() == null || mBinder.getLatestMediaServer().player == null)
						throw new RemoteException(getResources().getString(R.string.mediaserver_no_server),
								getResources().getString(R.string.mediaserver_no_server));
					else if (action.equals(RemoteWidgetProvider.ACTION_PLAY))
						mBinder.getLatestMediaServer().player.playPause();
					else if (action.equals(RemoteWidgetProvider.ACTION_STOP))
						mBinder.getLatestMediaServer().player.quit();
					else if (action.equals(RemoteWidgetProvider.ACTION_NEXT))
						mBinder.getLatestMediaServer().player.next();
					else if (action.equals(RemoteWidgetProvider.ACTION_PREV))
						mBinder.getLatestMediaServer().player.previous();
					else if (action.equals(RemoteWidgetProvider.ACTION_VOLDOWN))
						mBinder.getLatestMediaServer().player.volDown();
					else if (action.equals(RemoteWidgetProvider.ACTION_VOLUP))
						mBinder.getLatestMediaServer().player.volUp();
					else if (action.equals(RemoteWidgetProvider.ACTION_VOLUME)) {
						int volume = (int) (100 * intent.getDoubleExtra(RemoteWidgetProvider.EXTRA_VOLUME, 0.5));
						mBinder.getLatestMediaServer().player.setVolume(volume);
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

	private void initializeMusicWidgets() {
		if (mBinder != null && mBinder.isConnected())
			updateMusicWidget(null);
		else
			setMusicWidgetText(getResources().getString(R.string.no_conneciton),
					getResources().getString(R.string.no_conneciton_with_server), "", false, null);
	}

	protected void setMusicWidgetText(String big, String small, String small2, boolean playing, Bitmap thumbnail) {
		ComponentName thisWidget = new ComponentName(getApplicationContext(), RemoteWidgetProvider.class);
		// for (RemoteViews remote : remoteViewsList)
		RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(),
				R.layout.mediaserver_widget);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());
		if (remoteViews != null) {
			remoteViews.setTextViewText(R.id.lbl_widget_big, big);
			remoteViews.setTextViewText(R.id.lbl_widget_small, small);
			remoteViews.setTextViewText(R.id.lbl_widget_small2, small2);
			if (thumbnail != null) {
				remoteViews.setImageViewBitmap(R.id.img_widget_thumbnail, thumbnail);
				remoteViews.setViewVisibility(R.id.img_widget_thumbnail, View.VISIBLE);
			} else
				remoteViews.setViewVisibility(R.id.img_widget_thumbnail, View.INVISIBLE);
			RemoteWidgetProvider.setWidgetClick(remoteViews, this);
			if (playing)
				remoteViews.setInt(R.id.button_widget_play, "setBackgroundResource", R.drawable.player_pause);
			else
				remoteViews.setInt(R.id.button_widget_play, "setBackgroundResource", R.drawable.player_play);
			appWidgetManager.updateAppWidget(thisWidget, remoteViews);
			if (DEBUGGING)
				Toast.makeText(this, small, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onDestroy() {
		if (mBinder != null)
			mBinder.removeRemoteActionListener(this);
		unbindService(playerConnection);
		super.onDestroy();
	}

	@Override
	public void onPlayingBeanChanged(String mediaserver, PlayingBean bean) {
		updateMusicWidget(bean);
	}

	@Override
	public void onServerConnectionChanged(RemoteServer server) {
		initializeMusicWidgets();
		updateSwitchWidget();
	}

	@Override
	public void startReceive(long size, String file) {
		// TODO Auto-generated method stub

	}

	@Override
	public void progressReceive(long size, String file) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endReceive(long size) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exceptionOccurred(Exception e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void downloadCanceled() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopService() {
		setMusicWidgetText(getResources().getString(R.string.no_conneciton),
				getResources().getString(R.string.no_conneciton_with_server), "", false, null);
		updateSwitchWidget();
		stopSelf();
	}

	@Override
	public void onPowerSwitchChange(String switchId, State state) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());

		ComponentName thisWidget = new ComponentName(getApplicationContext(), RemotePowerWidgetProvider.class);
		final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		SharedPreferences prefs = getSharedPreferences(SelectSwitchActivity.WIDGET_PREFS, 0);

		// update each of the app widgets with the remote adapter
		for (int i = 0; i < appWidgetIds.length; ++i) {

			String id = prefs.getString(appWidgetIds[i] + "", null);
			BufferdUnit bufferdUnit = mBinder.getSwitches().get(id);
			if (switchId.equals(id) && bufferdUnit != null) {
				updateSwitchWidget(appWidgetIds[i], getImageForSwitchType(bufferdUnit.mSwitchType, state == State.ON),
						bufferdUnit.mName);
			}
		}
	}

	private void updateSwitchWidget() {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());

		ComponentName thisWidget = new ComponentName(getApplicationContext(), RemotePowerWidgetProvider.class);
		final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		// update each of the app widgets with the remote adapter
		Thread thread = new Thread() {
			public void run() {
				for (int i = 0; i < appWidgetIds.length; ++i) {
					try {
						updateSwitchWidget(appWidgetIds[i]);
					} catch (Exception e) {
						// e.printStackTrace();
						System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
					}
				}
			};
		};
		thread.start();
	}

	private void updateSwitchWidget(int widgetID) throws Exception {
		if (mBinder == null)
			throw new Exception(getResources().getString(R.string.no_conneciton));
		SharedPreferences prefs = getSharedPreferences(SelectSwitchActivity.WIDGET_PREFS, 0);
		String switchID = prefs.getString(widgetID + "", null);
		if (switchID == null)
			return;
		BufferdUnit unit = mBinder.getSwitches().get(switchID);
		if (unit == null) {
			updateSwitchWidget(widgetID, R.drawable.switch_unknown, switchID);
			throw new Exception(mBinder.getServer().getName() + " "
					+ getResources().getString(R.string.switch_has_no_switch) + " " + switchID);
		}
		IInternetSwitch power = (IInternetSwitch) unit.mObject;
		State state = power.getState();
		updateSwitchWidget(widgetID, getImageForSwitchType(unit.mSwitchType, state == State.ON), unit.mName);
	}

	private void updateSwitchWidget(int widgetID, int image, String text) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());
		RemoteViews remoteSwitchViews = new RemoteViews(getApplicationContext().getPackageName(),
				R.layout.switch_widget);
		remoteSwitchViews.setImageViewResource(R.id.image_power_widget, image);
		remoteSwitchViews.setTextViewText(R.id.text_power_widget, text);

		RemotePowerWidgetProvider.setSwitchIntent(remoteSwitchViews, this, widgetID);

		appWidgetManager.updateAppWidget(widgetID, remoteSwitchViews);
	}

	private void switchPower(final int widgetID) throws Exception {
		refreshWebApi();
		SharedPreferences prefs = getSharedPreferences(SelectSwitchActivity.WIDGET_PREFS, 0);
		final String switchID = prefs.getString(widgetID + "", null);
		if (switchID == null)
			return;
		for (BeanSwitch bSwitch : mWebSwitch.getSwitches()) {
			if (bSwitch.getID().equals(switchID)) {
				String newState = (bSwitch.getState().equals("ON") ? "OFF" : "ON");
				mWebSwitch.setSwitchState(bSwitch.getID(), newState);

				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());
				RemoteViews remoteSwitchViews = new RemoteViews(getApplicationContext().getPackageName(),
						R.layout.switch_widget);
				remoteSwitchViews.setImageViewResource(R.id.image_power_widget,
						getImageForSwitchType(bSwitch.getType(), newState.equals("ON")));
				remoteSwitchViews.setTextViewText(R.id.text_power_widget, bSwitch.getName());
				RemotePowerWidgetProvider.setSwitchIntent(remoteSwitchViews, this, widgetID);
				appWidgetManager.updateAppWidget(widgetID, remoteSwitchViews);
			}
		}

	}

	@Override
	public void startSending(long size) {
		// TODO Auto-generated method stub

	}

	@Override
	public void progressSending(long size) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endSending(long size) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendingCanceled() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onControlUnitCreated(BufferdUnit controlUnit) {
	}

	public void onGroundPlotCreated(de.neo.remote.api.GroundPlot plot) {
	};

	public int getImageForSwitchType(String type, boolean on) {
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
}
