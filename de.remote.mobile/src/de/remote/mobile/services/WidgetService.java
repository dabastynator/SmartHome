package de.remote.mobile.services;

import java.util.ArrayList;
import java.util.List;

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
import de.remote.api.PlayingBean.STATE;
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
	private List<RemoteViews> remoteViewsList = new ArrayList<RemoteViews>();

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
			binder.addRemoteActionListener(WidgetService.this);
			if (binder.isConnected())
				updateWidget();
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
	};

	protected void updateWidget() {
		PlayingBean playing = null;
		if (binder.getPlayer() != null)
			playing = binder.getPlayingFile();
		if (playing == null) {
			setWidgetText("no file playing", "", "", false);
			return;
		}
		String title = "playing";
		if (playing.getTitle() != null)
			title = playing.getTitle();
		else if (playing.getFile() != null)
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
			configureWidgets();
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
					if (binder.getPlayer() == null)
						throw new RemoteException("not connected",
								"not connected");
					else if (action.equals(RemoteWidgetProvider.ACTION_PLAY))
						binder.getPlayer().playPause();
					else if (action.equals(RemoteWidgetProvider.ACTION_STOP))
						binder.getPlayer().quit();
					else if (action.equals(RemoteWidgetProvider.ACTION_NEXT))
						binder.getPlayer().next();
					else if (action.equals(RemoteWidgetProvider.ACTION_PREV))
						binder.getPlayer().previous();
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
		if (binder != null) {
			if (binder.isConnected())
				updateWidget();
			else
				setWidgetText("not connected", "no connection with any server",
						"", false);
		}
	}

	private void configureWidgets() {
		RemoteViews remoteViews = new RemoteViews(getApplicationContext()
				.getPackageName(), R.layout.widget);
		remoteViewsList.add(remoteViews);
	}

	protected void setWidgetText(String big, String small, String small2,
			boolean playing) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());
		ComponentName thisWidget = new ComponentName(getApplicationContext(),
				RemoteWidgetProvider.class);
		for (RemoteViews remote : remoteViewsList) {
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
	public void newPlayingFile(PlayingBean bean) {
		updateWidget();
	}

	@Override
	public void serverConnectionChanged(String serverName) {
		if (serverName == null)
			setWidgetText("no connection", "", "", false);
		else
			updateWidget();
	}

}
