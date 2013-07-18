package de.remote.mobile.services;

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
import de.remote.api.PlayingBean;
import de.remote.gpiopower.api.IGPIOPower.State;
import de.remote.gpiopower.api.IGPIOPower.Switch;
import de.remote.mobile.R;
import de.remote.mobile.activities.PowerActivity;
import de.remote.mobile.activities.SelectSwitchActivity;
import de.remote.mobile.database.RemoteDatabase;
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

	private RemoteDatabase serverDB;

	@Override
	public void onCreate() {
		// bind service
		serverDB = new RemoteDatabase(this);
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
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
		};
		thread.start();
	}

	private void updateWidget(int widgetID) throws Exception {
		if (binder == null)
			throw new Exception("not conneced");
		if (binder.getPower() == null)
			throw new Exception(binder.getServerName() + " has no power server");
		SharedPreferences prefs = getSharedPreferences(
				SelectSwitchActivity.WIDGET_PREFS, 0);
		int switcH = prefs.getInt(widgetID + "", -1);
		if (switcH == -1)
			return;
		String name = serverDB.getPowerSwitchDao().getNameOfSwitch(switcH);
		if (name == null)
			name = "Switch " + switcH;
		Switch s = Switch.values()[switcH];
		State state = binder.getPower().getState(s);
		if (state == State.ON)
			updateWidget(widgetID, R.drawable.light_on, name);
		else
			updateWidget(widgetID, R.drawable.light_off, name);
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
		if (binder.getPower() == null)
			throw new Exception(binder.getServerName() + " has no power server");
		RemoteDatabase serverDB = new RemoteDatabase(this);
		SharedPreferences prefs = getSharedPreferences(
				SelectSwitchActivity.WIDGET_PREFS, 0);
		int switcH = prefs.getInt(widgetID + "", -1);
		if (switcH == -1)
			return;
		String name = serverDB.getPowerSwitchDao().getNameOfSwitch(switcH);
		if (name == null)
			name = "Switch " + switcH;
		Switch s = Switch.values()[switcH];
		State state = binder.getPower().getState(s);
		if (state == State.ON)
			state = State.OFF;
		else
			state = State.ON;
		binder.getPower().setState(state, s);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());
		ComponentName thisWidget = new ComponentName(getApplicationContext(),
				RemotePowerWidgetProvider.class);
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
		serverDB.close();
		if (binder != null)
			binder.removeRemoteActionListener(this);
		unbindService(playerConnection);
		super.onDestroy();
	}

	@Override
	public void onPlayingBeanChanged(PlayingBean bean) {
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
	public void onPowerSwitchChange(Switch _switch, State state) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());

		ComponentName thisWidget = new ComponentName(getApplicationContext(),
				RemotePowerWidgetProvider.class);
		final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		SharedPreferences prefs = getSharedPreferences(
				SelectSwitchActivity.WIDGET_PREFS, 0);

		// update each of the app widgets with the remote adapter
		for (int i = 0; i < appWidgetIds.length; ++i) {

			int switcH = prefs.getInt(appWidgetIds[i] + "", -1);
			if (switcH == _switch.ordinal()) {
				String name = serverDB.getPowerSwitchDao().getNameOfSwitch(
						switcH);
				if (name == null)
					name = "Switch " + switcH;
				if (state == State.ON)
					updateWidget(appWidgetIds[i], R.drawable.light_on, name);
				else
					updateWidget(appWidgetIds[i], R.drawable.light_off, name);
			}
		}
	}

}