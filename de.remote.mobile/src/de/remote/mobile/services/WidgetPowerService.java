package de.remote.mobile.services;

import java.util.Map;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.Toast;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.gpiopower.api.IInternetSwitch;
import de.remote.gpiopower.api.IInternetSwitch.State;
import de.remote.mediaserver.api.PlayingBean;
import de.remote.mobile.R;
import de.remote.mobile.activities.PowerActivity;
import de.remote.mobile.activities.SelectSwitchActivity;
import de.remote.mobile.receivers.RemotePowerWidgetProvider;
import de.remote.mobile.services.RemoteService.IRemoteActionListener;

/**
 * this service updates a widget and handles actions on the widget.
 * 
 * @author sebastian
 */
public class WidgetPowerService extends Service implements
		IRemoteActionListener {

	/**
	 * binder for connection with the remote service
	 */
	private PlayerBinder binder;

	private RemoteViews remotePowerViews;

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
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (PlayerBinder) service;
			binder.addRemoteActionListener(WidgetPowerService.this);
		}
	};

	@Override
	public void onCreate() {
		// bind service
		Intent intent = new Intent(this, RemoteService.class);
		startService(intent);
		bindService(intent, playerConnection, Context.BIND_AUTO_CREATE);
		remotePowerViews = new RemoteViews(getApplicationContext()
				.getPackageName(), R.layout.power_widget);
		updateWidget();
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null && intent.getAction() != null) {
			int widgetID = intent.getIntExtra(PowerActivity.SWITCH_NUMBER, 0);
			executeCommand(intent.getAction(), widgetID);
		} else {
			remotePowerViews = new RemoteViews(getApplicationContext()
					.getPackageName(), R.layout.power_widget);
			updateWidget();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void updateWidget() {
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
						updateWidget(appWidgetIds[i]);
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

	private void updateWidget(int widgetID) throws Exception {
		if (binder == null)
			throw new Exception("not conneced");
		Map<String, IInternetSwitch> powers = binder.getPower();
		SharedPreferences prefs = getSharedPreferences(
				SelectSwitchActivity.WIDGET_PREFS, 0);
		String switchName = prefs.getString(widgetID + "", null);
		if (switchName == null)
			return;
		updateWidget(widgetID, R.drawable.light_off, switchName);
		if (powers == null)
			throw new Exception(binder.getServerName() + " has no power server");
		IInternetSwitch power = powers.get(switchName);
		if (power == null)
			throw new Exception(binder.getServerName()
					+ " has no switch called " + switchName);
		State state = power.getState();
		if (state == State.ON)
			updateWidget(widgetID, R.drawable.light_on, switchName);
		else
			updateWidget(widgetID, R.drawable.light_off, switchName);
	}

	private void updateWidget(int widgetID, int image, String text) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());
		if (remotePowerViews != null) {
			remotePowerViews.setImageViewResource(R.id.image_power_widget,
					image);
			remotePowerViews.setTextViewText(R.id.text_power_widget, text);
			appWidgetManager.updateAppWidget(widgetID, remotePowerViews);
		}
	}

	/**
	 * execute given action
	 * 
	 * @param action
	 * @param widgetID
	 * @return true if action is known
	 */
	private void executeCommand(final String action, final int widgetID) {
		new Thread() {
			public void run() {
				try {
					if (binder == null)
						throw new RemoteException("not binded", "not binded");
					else if (action
							.equals(RemotePowerWidgetProvider.ACTION_SWITCH))
						switchPower(widgetID);
				} catch (final Exception e) {
					handler.post(new Runnable() {
						public void run() {
							Toast.makeText(WidgetPowerService.this,
									e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}.start();
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
		if (remotePowerViews != null) {
			if (state == State.ON)
				remotePowerViews.setImageViewResource(R.id.image_power_widget,
						R.drawable.light_on);
			else
				remotePowerViews.setImageViewResource(R.id.image_power_widget,
						R.drawable.light_off);
			remotePowerViews.setTextViewText(R.id.text_power_widget, name);
			appWidgetManager.updateAppWidget(widgetID, remotePowerViews);
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
	}

	@Override
	public void onServerConnectionChanged(String serverName, int serverID) {
		updateWidget();
	}

	@Override
	public void startReceive(long size) {
		// TODO Auto-generated method stub

	}

	@Override
	public void progressReceive(long size) {
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
					updateWidget(appWidgetIds[i], R.drawable.light_on, name);
				else
					updateWidget(appWidgetIds[i], R.drawable.light_off, name);
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

}
