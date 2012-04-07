package de.remote.mobile.services;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.PlayerException;
import de.remote.api.PlayingBean;
import de.remote.mobile.R;
import de.remote.mobile.receivers.RemoteWidgetProvider;
import de.remote.mobile.services.RemoteService.IRemoteActionListener;
import de.remote.mobile.services.RemoteService.PlayerBinder;

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
				setWidgetText("not connected", "no connection with any server","");

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
		try {
			if (binder.getPlayer() != null)
				playing = binder.getPlayer().getPlayingFile();
		} catch (RemoteException e) {
		} catch (PlayerException e) {
		}
		if (playing == null) {
			setWidgetText("no file playing", "","");
			return;
		}
		String title = "playing";
		if (playing.getTitle() != null)
			title = playing.getTitle();
		String author = "";
		if (playing.getArtist() != null)
			author = playing.getArtist();
		String album = "";
		if (playing.getAlbum() != null)
			album = playing.getAlbum();
		setWidgetText(title, author, album);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null || intent.getAction() == null
				|| !executeCommand(intent.getAction())) {
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
	private boolean executeCommand(String action) {
		try {
			if (binder == null)
				throw new RemoteException("not binded", "not binded");
			if (binder.getPlayer() == null)
				throw new RemoteException("not connected", "not connected");
			else if (action.equals(RemoteWidgetProvider.ACTION_PLAY))
				binder.getPlayer().playPause();
			else if (action.equals(RemoteWidgetProvider.ACTION_STOP))
				binder.getPlayer().quit();
			else if (action.equals(RemoteWidgetProvider.ACTION_NEXT))
				binder.getPlayer().next();
			else if (action.equals(RemoteWidgetProvider.ACTION_PREV))
				binder.getPlayer().previous();
			else
				return false;
		} catch (RemoteException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			return false;
		} catch (PlayerException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	private void initializeWidgets() {
		if (binder != null) {
			if (binder.isConnected())
				updateWidget();
			else
				setWidgetText("not connected", "no connection with any server","");
		}
	}

	private void configureWidgets() {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());

		ComponentName thisWidget = new ComponentName(getApplicationContext(),
				RemoteWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int widgetId : allWidgetIds) {
			// Create some random data

			RemoteViews remoteViews = new RemoteViews(getApplicationContext()
					.getPackageName(), R.layout.widget);
			remoteViewsList.add(remoteViews);
			// Set the text

			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
	}

	protected void setWidgetText(String big, String small, String small2) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());
		ComponentName thisWidget = new ComponentName(getApplicationContext(),
				RemoteWidgetProvider.class);
		for (RemoteViews remote : remoteViewsList) {
			remote.setTextViewText(R.id.lbl_widget_big, big);
			remote.setTextViewText(R.id.lbl_widget_small, small);
			remote.setTextViewText(R.id.lbl_widget_small2, small2);
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
		super.onDestroy();
	}

	@Override
	public void newPlayingFile(PlayingBean bean) {
		updateWidget();
	}

	@Override
	public void serverConnectionChanged(String serverName) {
		if (serverName != null)
			Log.e("new server", serverName);
		else
			Log.e("new server", "disconnect");
		if (serverName == null)
			setWidgetText("no connection", "","");
		else
			updateWidget();
	}

}
