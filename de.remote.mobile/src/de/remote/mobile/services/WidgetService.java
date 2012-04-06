package de.remote.mobile.services;

import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
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
			Log.e("wi service", "binded");
			binder = (PlayerBinder) service;
			binder.addRemoteActionListener(WidgetService.this);
			if (binder.isConnected())
				updateWidget();
			else
				setWidgetText("not connected", "no connection with any server");
		}
	};

	@Override
	public void onCreate() {
		// bind service
		Log.e("wi service", "create");
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
			setWidgetText("no file playing", "");
			return;
		}
		StringBuilder sb = new StringBuilder();
		String t = "Playing";
		if (playing.getTitle() != null && playing.getTitle().length() > 0)
			t = playing.getTitle();
		if (playing.getArtist() != null && playing.getArtist().length() > 0)
			sb.append(playing.getArtist());
		if (playing.getAlbum() != null && playing.getAlbum().length() > 0)
			sb.append(" <" + playing.getAlbum() + ">");
		String msg = sb.toString();
		String title = t;
		setWidgetText(title, msg);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());

		ComponentName thisWidget = new ComponentName(getApplicationContext(),
				RemoteWidgetProvider.class);

		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		int i = 0;
		for (int widgetId : allWidgetIds) {
			// Create some random data

			RemoteViews remoteViews = new RemoteViews(this
					.getApplicationContext().getPackageName(), R.layout.widget);
			// Set the text

			// Register an onClickListener
			Intent clickIntent = new Intent(this.getApplicationContext(),
					RemoteWidgetProvider.class);


			remoteViewsList.add(remoteViews);
			clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
					allWidgetIds);

			PendingIntent pendingIntent = PendingIntent.getBroadcast(
					getApplicationContext(), 0, clickIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.button_widget_next,
					pendingIntent);
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
		if (binder != null) {
			if (binder.isConnected())
				updateWidget();
			else
				setWidgetText("not connected", "no connection with any server");
		}

		return super.onStartCommand(intent, flags, startId);
	}

	protected void setWidgetText(String big, String small) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());
		ComponentName thisWidget = new ComponentName(getApplicationContext(),
				RemoteWidgetProvider.class);
		for (RemoteViews remote : remoteViewsList) {
			remote.setTextViewText(R.id.lbl_widget_big, big);
			remote.setTextViewText(R.id.lbl_widget_small, small);
			appWidgetManager.updateAppWidget(thisWidget, remote);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		if (binder != null)
			binder.removeRemoteActionListener(this);
		super.onDestroy();
	}

	@Override
	public void newPlayingFile(PlayingBean bean) {
		Log.e("widget","new playing");
		updateWidget();
	}

	@Override
	public void serverConnectionChanged(String serverName) {
		if (serverName == null)
			setWidgetText("no connection", "");
		else
			updateWidget();
	}

}
