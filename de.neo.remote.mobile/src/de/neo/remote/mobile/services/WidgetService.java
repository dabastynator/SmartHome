package de.neo.remote.mobile.services;

import java.nio.IntBuffer;
import java.util.Map;

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
import de.neo.remote.gpiopower.api.IInternetSwitch;
import de.neo.remote.gpiopower.api.IInternetSwitch.State;
import de.neo.remote.mediaserver.api.PlayingBean;
import de.neo.remote.mediaserver.api.PlayingBean.STATE;
import de.neo.remote.mobile.activities.SelectSwitchActivity;
import de.neo.remote.mobile.receivers.RemotePowerWidgetProvider;
import de.neo.remote.mobile.receivers.RemoteWidgetProvider;
import de.neo.remote.mobile.services.RemoteService.IRemoteActionListener;
import de.neo.rmi.protokol.RemoteException;
import de.remote.mobile.R;

/**
 * this service updates a widget and handles actions on the widget.
 * 
 * @author sebastian
 */
public class WidgetService extends Service implements IRemoteActionListener {

	/**
	 * binder for connection with the remote service
	 */
	private PlayerBinder binder;

	/**
	 * list of all remote views
	 */
	private RemoteViews remoteViews;

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
			binder = (PlayerBinder) service;
			binder.addRemoteActionListener(WidgetService.this);
			if (binder.isConnected())
				updateWidget(null);
			else
				setWidgetText("not connected", "no connection with any server",
						"", false, null);

		}
	};

	private RemoteViews remoteSwitchViews;

	@Override
	public void onCreate() {
		// bind service
		Intent intent = new Intent(this, RemoteService.class);
		startService(intent);
		bindService(intent, playerConnection, Context.BIND_AUTO_CREATE);
		remoteViews = new RemoteViews(getApplicationContext().getPackageName(),
				R.layout.mediaserver_widget);
		initializeWidgets();
		remoteSwitchViews = new RemoteViews(getApplicationContext()
				.getPackageName(), R.layout.switch_widget);
		updateSwitchWidget();
	};

	protected void updateWidget(PlayingBean playing) {
		if (binder.getLatestMediaServer() == null) {
			setWidgetText(
					"no music station",
					"no music station specified at server "
							+ binder.getServerName(), "", false, null);
			return;
		}
		if (binder.getLatestMediaServer().player != null && playing == null)
			playing = binder.getPlayingFile();
		if (playing == null || playing.getState() == STATE.DOWN) {
			setWidgetText("no file playing",
					"at music station " + binder.getLatestMediaServer().name,
					"", false, null);
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
		setWidgetText(title, author, album, playing.getState() == STATE.PLAY,
				thumbnail);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null && intent.getAction() != null)
			executeCommand(intent.getAction(), intent);
		else {
			remoteViews = new RemoteViews(getApplicationContext()
					.getPackageName(), R.layout.mediaserver_widget);
			initializeWidgets();
			remoteSwitchViews = new RemoteViews(getApplicationContext()
					.getPackageName(), R.layout.switch_widget);
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
						throw new RemoteException("no mediaplayer selected",
								"no mediaplayer selected");
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

	private void initializeWidgets() {
		if (binder != null && binder.isConnected())
			updateWidget(null);
		else
			setWidgetText("not connected", "no connection with any server", "",
					false, null);
	}

	protected void setWidgetText(String big, String small, String small2,
			boolean playing, Bitmap thumbnail) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());
		ComponentName thisWidget = new ComponentName(getApplicationContext(),
				RemoteWidgetProvider.class);
		// for (RemoteViews remote : remoteViewsList)
		RemoteViews remote = remoteViews;
		if (remote != null) {
			remote.setTextViewText(R.id.lbl_widget_big, big);
			remote.setTextViewText(R.id.lbl_widget_small, small);
			remote.setTextViewText(R.id.lbl_widget_small2, small2);
			if (thumbnail != null)
				remote.setImageViewBitmap(R.id.img_widget_thumbnail, thumbnail);
			else
				remote.setImageViewResource(R.id.img_widget_thumbnail,
						R.drawable.audio);
			RemoteWidgetProvider.setWidgetClick(remote, this);
			if (playing)
				remote.setInt(R.id.button_widget_play, "setBackgroundResource",
						R.drawable.player_pause);
			else
				remote.setInt(R.id.button_widget_play, "setBackgroundResource",
						R.drawable.player_play);
			appWidgetManager.updateAppWidget(thisWidget, remote);
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
		updateWidget(bean);
	}

	@Override
	public void onServerConnectionChanged(String serverName, int serverID) {
		initializeWidgets();
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
		setWidgetText("not connected", "no connection with any server", "",
				false, null);
		stopSelf();
	}

	@Override
	public void onPowerSwitchChange(String switchName, State state) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());

		ComponentName thisWidget = new ComponentName(getApplicationContext(),
				RemotePowerWidgetProvider.class);
		final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		SharedPreferences prefs = getSharedPreferences(
				SelectSwitchActivity.WIDGET_PREFS, 0);

		// update each of the app widgets with the remote adapter
		for (int i = 0; i < appWidgetIds.length; ++i) {

			String name = prefs.getString(appWidgetIds[i] + "", null);
			if (switchName.equals(name)) {
				if (state == State.ON)
					updateSwitchWidget(appWidgetIds[i], R.drawable.light_on,
							name);
				else
					updateSwitchWidget(appWidgetIds[i], R.drawable.light_off,
							name);
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
			throw new Exception("not conneced");
		Map<String, IInternetSwitch> powers = binder.getPower();
		SharedPreferences prefs = getSharedPreferences(
				SelectSwitchActivity.WIDGET_PREFS, 0);
		String switchName = prefs.getString(widgetID + "", null);
		if (switchName == null)
			return;
		updateSwitchWidget(widgetID, R.drawable.light_off, switchName);
		if (powers == null)
			throw new Exception(binder.getServerName() + " has no power server");
		IInternetSwitch power = powers.get(switchName);
		if (power == null)
			throw new Exception(binder.getServerName()
					+ " has no switch called " + switchName);
		State state = power.getState();
		if (state == State.ON)
			updateSwitchWidget(widgetID, R.drawable.light_on, switchName);
		else
			updateSwitchWidget(widgetID, R.drawable.light_off, switchName);
	}

	private void updateSwitchWidget(int widgetID, int image, String text) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());
		if (remoteSwitchViews != null) {
			remoteSwitchViews.setImageViewResource(R.id.image_power_widget,
					image);
			remoteSwitchViews.setTextViewText(R.id.text_power_widget, text);
			RemotePowerWidgetProvider.setSwitchIntent(remoteSwitchViews, this,
					widgetID);
			appWidgetManager.updateAppWidget(widgetID, remoteSwitchViews);
		}
	}

	private void switchPower(int widgetID) throws Exception {
		if (binder == null)
			throw new Exception("not connected");
		Map<String, IInternetSwitch> powers = binder.getPower();
		SharedPreferences prefs = getSharedPreferences(
				SelectSwitchActivity.WIDGET_PREFS, 0);
		String name = prefs.getString(widgetID + "", null);
		if (name == null)
			return;
		IInternetSwitch power = powers.get(name);
		if (power == null)
			throw new Exception("Switch " + name + " is unknown");
		State state = power.getState();
		if (state == State.ON)
			state = State.OFF;
		else
			state = State.ON;
		power.setState(state);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());
		if (remoteSwitchViews != null) {
			if (state == State.ON)
				remoteSwitchViews.setImageViewResource(R.id.image_power_widget,
						R.drawable.light_on);
			else
				remoteSwitchViews.setImageViewResource(R.id.image_power_widget,
						R.drawable.light_off);
			remoteSwitchViews.setTextViewText(R.id.text_power_widget, name);
			appWidgetManager.updateAppWidget(widgetID, remoteSwitchViews);
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

}
