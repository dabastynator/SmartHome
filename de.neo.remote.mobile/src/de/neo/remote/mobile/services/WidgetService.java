package de.neo.remote.mobile.services;

import java.nio.IntBuffer;

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
import android.widget.RemoteViews;
import android.widget.Toast;
import de.neo.remote.api.IInternetSwitch;
import de.neo.remote.api.IInternetSwitch.State;
import de.neo.remote.api.PlayingBean;
import de.neo.remote.api.PlayingBean.STATE;
import de.neo.remote.mobile.activities.SelectSwitchActivity;
import de.neo.remote.mobile.persistence.RemoteServer;
import de.neo.remote.mobile.receivers.RemotePowerWidgetProvider;
import de.neo.remote.mobile.receivers.RemoteWidgetProvider;
import de.neo.remote.mobile.services.RemoteService.BufferdUnit;
import de.neo.remote.mobile.services.RemoteService.IRemoteActionListener;
import de.neo.remote.mobile.util.ControlSceneRenderer;
import de.neo.rmi.protokol.RemoteException;
import de.remote.mobile.R;

/**
 * this service updates a widget and handles actions on the widget.
 * 
 * @author sebastian
 */
public class WidgetService extends Service implements IRemoteActionListener {

	public static boolean DEBUGGING = false;

	/**
	 * binder for connection with the remote service
	 */
	private RemoteBinder binder;

	/**
	 * the handler executes runnables in the ui thread
	 */
	private Handler handler = new Handler();

	/**
	 * connection to the service
	 */
	private ServiceConnection playerConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			binder.removeRemoteActionListener(WidgetService.this);
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (RemoteBinder) service;
			binder.addRemoteActionListener(WidgetService.this);
			if (binder.isConnected())
				updateMusicWidget(null);
			else
				setMusicWidgetText(
						getResources().getString(R.string.no_conneciton),
						getResources().getString(
								R.string.no_conneciton_with_server), "", false,
						null);
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

	protected void updateMusicWidget(PlayingBean playing) {
		if (binder.getLatestMediaServer() == null) {
			setMusicWidgetText(
					getResources().getString(R.string.mediaserver_no_server),
					getResources().getString(R.string.mediaserver_no_server)
							+ " (" + binder.getServer().getName() + ")", "",
					false, null);
			return;
		}
		if (binder.getLatestMediaServer().player != null && playing == null)
			playing = binder.getPlayingFile();
		if (playing == null || playing.getState() == STATE.DOWN) {
			setMusicWidgetText(
					getResources().getString(R.string.player_no_file_playing),
					binder.getLatestMediaServer().name, "", false, null);
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
		if (playing.getThumbnailWidth() * playing.getThumbnailHeight() > 0
				&& playing.getThumbnailRGB() != null) {
			thumbnail = Bitmap.createBitmap(playing.getThumbnailWidth(),
					playing.getThumbnailHeight(), Bitmap.Config.RGB_565);
			IntBuffer buf = IntBuffer.wrap(playing.getThumbnailRGB()); // data
																		// is my
																		// array
			thumbnail.copyPixelsFromBuffer(buf);
		}
		setMusicWidgetText(title, author, album,
				playing.getState() == STATE.PLAY, thumbnail);
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
					if (binder == null)
						throw new RemoteException("not binded", "not binded");
					if (action.equals(RemotePowerWidgetProvider.ACTION_SWITCH)) {
						int widgetID = intent.getIntExtra(
								SelectSwitchActivity.SWITCH_NUMBER, 0);
						switchPower(widgetID);
						return;
					}
					if (binder.getLatestMediaServer() == null
							|| binder.getLatestMediaServer().player == null)
						throw new RemoteException(getResources().getString(
								R.string.mediaserver_no_server), getResources()
								.getString(R.string.mediaserver_no_server));
					else if (action.equals(RemoteWidgetProvider.ACTION_PLAY))
						binder.getLatestMediaServer().player.playPause();
					else if (action.equals(RemoteWidgetProvider.ACTION_STOP))
						binder.getLatestMediaServer().player.quit();
					else if (action.equals(RemoteWidgetProvider.ACTION_NEXT))
						binder.getLatestMediaServer().player.next();
					else if (action.equals(RemoteWidgetProvider.ACTION_PREV))
						binder.getLatestMediaServer().player.previous();
					else if (action.equals(RemoteWidgetProvider.ACTION_VOLDOWN))
						binder.getLatestMediaServer().player.volDown();
					else if (action.equals(RemoteWidgetProvider.ACTION_VOLUP))
						binder.getLatestMediaServer().player.volUp();
				} catch (final Exception e) {
					e.printStackTrace();
					handler.post(new Runnable() {
						public void run() {
							Toast.makeText(WidgetService.this, e.getMessage(),
									Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}.start();
	}

	private void initializeMusicWidgets() {
		if (binder != null && binder.isConnected())
			updateMusicWidget(null);
		else
			setMusicWidgetText(
					getResources().getString(R.string.no_conneciton),
					getResources()
							.getString(R.string.no_conneciton_with_server), "",
					false, null);
	}

	protected void setMusicWidgetText(String big, String small, String small2,
			boolean playing, Bitmap thumbnail) {
		ComponentName thisWidget = new ComponentName(getApplicationContext(),
				RemoteWidgetProvider.class);
		// for (RemoteViews remote : remoteViewsList)
		RemoteViews remoteViews = new RemoteViews(getApplicationContext()
				.getPackageName(), R.layout.mediaserver_widget);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());
		if (remoteViews != null) {
			remoteViews.setTextViewText(R.id.lbl_widget_big, big);
			remoteViews.setTextViewText(R.id.lbl_widget_small, small);
			remoteViews.setTextViewText(R.id.lbl_widget_small2, small2);
			if (thumbnail != null)
				remoteViews.setImageViewBitmap(R.id.img_widget_thumbnail,
						thumbnail);
			else
				remoteViews.setImageViewResource(R.id.img_widget_thumbnail,
						R.drawable.audio);
			RemoteWidgetProvider.setWidgetClick(remoteViews, this);
			if (playing)
				remoteViews.setInt(R.id.button_widget_play,
						"setBackgroundResource", R.drawable.player_pause);
			else
				remoteViews.setInt(R.id.button_widget_play,
						"setBackgroundResource", R.drawable.player_play);
			appWidgetManager.updateAppWidget(thisWidget, remoteViews);
			if (DEBUGGING)
				Toast.makeText(this, small, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onDestroy() {
		if (binder != null)
			binder.removeRemoteActionListener(this);
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
				getResources().getString(R.string.no_conneciton_with_server),
				"", false, null);
		updateSwitchWidget();
		stopSelf();
	}

	@Override
	public void onPowerSwitchChange(String switchId, State state) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());

		ComponentName thisWidget = new ComponentName(getApplicationContext(),
				RemotePowerWidgetProvider.class);
		final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		SharedPreferences prefs = getSharedPreferences(
				SelectSwitchActivity.WIDGET_PREFS, 0);

		// update each of the app widgets with the remote adapter
		for (int i = 0; i < appWidgetIds.length; ++i) {

			String id = prefs.getString(appWidgetIds[i] + "", null);
			BufferdUnit bufferdUnit = binder.getSwitches().get(id);
			if (switchId.equals(id) && bufferdUnit != null) {
				updateSwitchWidget(
						appWidgetIds[i],
						getImageForSwitchType(bufferdUnit.mSwitchType,
								state == State.ON), bufferdUnit.mName);
			}
		}
	}

	private void updateSwitchWidget() {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());

		ComponentName thisWidget = new ComponentName(getApplicationContext(),
				RemotePowerWidgetProvider.class);
		final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		// update each of the app widgets with the remote adapter
		Thread thread = new Thread() {
			public void run() {
				for (int i = 0; i < appWidgetIds.length; ++i) {
					try {
						updateSwitchWidget(appWidgetIds[i]);
					} catch (Exception e) {
						// e.printStackTrace();
						System.err.println(e.getClass().getSimpleName() + ": "
								+ e.getMessage());
					}
				}
			};
		};
		thread.start();
	}

	private void updateSwitchWidget(int widgetID) throws Exception {
		if (binder == null)
			throw new Exception(getResources()
					.getString(R.string.no_conneciton));
		SharedPreferences prefs = getSharedPreferences(
				SelectSwitchActivity.WIDGET_PREFS, 0);
		String switchID = prefs.getString(widgetID + "", null);
		if (switchID == null)
			return;
		BufferdUnit unit = binder.getSwitches().get(switchID);
		if (unit == null) {
			updateSwitchWidget(widgetID, R.drawable.switch_unknown, switchID);
			throw new Exception(binder.getServer().getName() + " "
					+ getResources().getString(R.string.switch_has_no_switch)
					+ " " + switchID);
		}
		IInternetSwitch power = (IInternetSwitch) unit.mObject;
		State state = power.getState();
		updateSwitchWidget(widgetID,
				getImageForSwitchType(unit.mSwitchType, state == State.ON),
				unit.mName);
	}

	private void updateSwitchWidget(int widgetID, int image, String text) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());
		RemoteViews remoteSwitchViews = new RemoteViews(getApplicationContext()
				.getPackageName(), R.layout.switch_widget);
		remoteSwitchViews.setImageViewResource(R.id.image_power_widget, image);
		remoteSwitchViews.setTextViewText(R.id.text_power_widget, text);

		RemotePowerWidgetProvider.setSwitchIntent(remoteSwitchViews, this,
				widgetID);

		appWidgetManager.updateAppWidget(widgetID, remoteSwitchViews);
	}

	private void switchPower(final int widgetID) throws Exception {
		if (binder == null)
			throw new Exception(getResources()
					.getString(R.string.no_conneciton));
		SharedPreferences prefs = getSharedPreferences(
				SelectSwitchActivity.WIDGET_PREFS, 0);
		final String switchID = prefs.getString(widgetID + "", null);
		if (switchID == null)
			return;
		BufferdUnit unit = binder.getSwitches().get(switchID);
		if (unit == null)
			throw new Exception(binder.getServer().getName() + " "
					+ getResources().getString(R.string.switch_has_no_switch)
					+ " " + switchID);
		if (DEBUGGING)
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(),
							"Switch " + switchID + " id=" + widgetID,
							Toast.LENGTH_SHORT).show();
				}
			});
		IInternetSwitch power = (IInternetSwitch) unit.mObject;
		State state = power.getState();
		if (state == State.ON)
			state = State.OFF;
		else
			state = State.ON;
		power.setState(state);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());
		RemoteViews remoteSwitchViews = new RemoteViews(getApplicationContext()
				.getPackageName(), R.layout.switch_widget);
		remoteSwitchViews.setImageViewResource(R.id.image_power_widget,
				getImageForSwitchType(unit.mSwitchType, state == State.ON));
		remoteSwitchViews.setTextViewText(R.id.text_power_widget, unit.mName);
		RemotePowerWidgetProvider.setSwitchIntent(remoteSwitchViews, this,
				widgetID);
		appWidgetManager.updateAppWidget(widgetID, remoteSwitchViews);
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
		if (ControlSceneRenderer.LAMP_FLOOR.equals(type)
				|| ControlSceneRenderer.LAMP_LAVA.equals(type)) {
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
		return R.drawable.switch_unknown;
	}
}
