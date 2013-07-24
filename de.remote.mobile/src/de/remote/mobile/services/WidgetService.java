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
import de.remote.gpiopower.api.IInternetSwitch.State;
import de.remote.mediaserver.api.PlayingBean;
import de.remote.mediaserver.api.PlayingBean.STATE;
import de.remote.mobile.R;
import de.remote.mobile.receivers.RemoteWidgetProvider;
import de.remote.mobile.services.RemoteService.IRemoteActionListener;

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
						"", false);

		}
	};

	@Override
	public void onCreate() {
		// bind service
		Intent intent = new Intent(this, RemoteService.class);
		startService(intent);
		bindService(intent, playerConnection, Context.BIND_AUTO_CREATE);
		remoteViews = new RemoteViews(getApplicationContext().getPackageName(),
				R.layout.widget);
		initializeWidgets();
	};

	protected void updateWidget(PlayingBean playing) {
		if (binder.getLatestMediaServer() == null) {
			setWidgetText(
					"no music station",
					"no music station specified at server "
							+ binder.getServerName(), "", false);
			return;
		}
		if (binder.getLatestMediaServer().player != null && playing == null)
			playing = binder.getPlayingFile();
		if (playing == null || playing.getState() == STATE.DOWN) {
			setWidgetText("no file playing",
					"at music station " + binder.getLatestMediaServer().name,
					"", false);
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
		setWidgetText(title, author, album, playing.getState() == STATE.PLAY);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null && intent.getAction() != null)
			executeCommand(intent.getAction());
		else {
			remoteViews = new RemoteViews(getApplicationContext()
					.getPackageName(), R.layout.widget);
			initializeWidgets();
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
					if (binder == null)
						throw new RemoteException("not binded", "not binded");
					if (binder.getLatestMediaServer().player == null)
						throw new RemoteException("not connected",
								"not connected");
					else if (action.equals(RemoteWidgetProvider.ACTION_PLAY))
						binder.getLatestMediaServer().player.playPause();
					else if (action.equals(RemoteWidgetProvider.ACTION_STOP))
						binder.getLatestMediaServer().player.quit();
					else if (action.equals(RemoteWidgetProvider.ACTION_NEXT))
						binder.getLatestMediaServer().player.next();
					else if (action.equals(RemoteWidgetProvider.ACTION_PREV))
						binder.getLatestMediaServer().player.previous();
				} catch (final Exception e) {
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
					false);
	}

	protected void setWidgetText(String big, String small, String small2,
			boolean playing) {
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
			if (playing)
				remote.setImageViewResource(R.id.button_widget_play,
						R.drawable.pause);
			else
				remote.setImageViewResource(R.id.button_widget_play,
						R.drawable.play);
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
	public void onPlayingBeanChanged(PlayingBean bean) {
		updateWidget(bean);
	}

	@Override
	public void onServerConnectionChanged(String serverName, int serverID) {
		initializeWidgets();
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
		setWidgetText("not connected", "no connection with any server", "",
				false);
		stopSelf();
	}

	@Override
	public void onPowerSwitchChange(String switchName, State state) {
		// TODO Auto-generated method stub

	}

}
