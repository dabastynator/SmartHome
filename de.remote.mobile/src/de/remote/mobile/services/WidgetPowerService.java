package de.remote.mobile.services;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.Toast;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.PlayingBean;
import de.remote.gpiopower.api.IGPIOPower.State;
import de.remote.gpiopower.api.IGPIOPower.Switch;
import de.remote.mobile.R;
import de.remote.mobile.receivers.RemotePowerWidgetProvider;
import de.remote.mobile.receivers.RemoteWidgetProvider;
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
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null && intent.getAction() != null)
			executeCommand(intent.getAction());
		else {
			remotePowerViews = new RemoteViews(getApplicationContext()
					.getPackageName(), R.layout.power_widget);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * execute given action
	 * 
	 * @param action
	 * @return true if action is known
	 */
	private void executeCommand(final String action) {
		new Thread() {
			public void run() {
				try {
					String abc = action;
					if (binder == null)
						throw new RemoteException("not binded", "not binded");
					else if (action
							.equals(RemotePowerWidgetProvider.ACTION_SWITCH))
						switchPower();
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

	private void switchPower() throws Exception {
		if (binder.getPower() == null)
			throw new Exception(binder.getServerName() + " has no power server");
		State state = binder.getPower().getState(Switch.A);
		if (state == State.ON)
			state = State.OFF;
		else
			state = State.ON;
		binder.getPower().setState(state, Switch.A);
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
			appWidgetManager.updateAppWidget(thisWidget, remotePowerViews);
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
	public void newPlayingFile(PlayingBean bean) {
	}

	@Override
	public void serverConnectionChanged(String serverName) {
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

}
