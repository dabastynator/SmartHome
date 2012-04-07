package de.remote.mobile.receivers;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import de.remote.mobile.R;
import de.remote.mobile.activies.BrowserActivity;
import de.remote.mobile.services.WidgetService;

/**
 * the remote widget provider starts the widget service.
 * 
 * @author sebastian
 */
public class RemoteWidgetProvider extends AppWidgetProvider {

	/**
	 * click on play action
	 */
	public static final String ACTION_PLAY = "de.remote.mobile.ACTION_PLAY";

	/**
	 * click on stop action
	 */
	public static final String ACTION_STOP = "de.remote.mobile.ACTION_STOP";

	/**
	 * click on next action
	 */
	public static final String ACTION_NEXT = "de.remote.mobile.ACTION_NEXT";

	/**
	 * click on prev action
	 */
	public static final String ACTION_PREV = "de.remote.mobile.ACTION_PREV";

	/**
	 * click on update action
	 */
	public static final String ACTION_UPDATE = "actionUpdate";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		Log.e("update", "update");
		Intent serviceIntent = new Intent(context, WidgetService.class);
		context.startService(serviceIntent);

		// update each of the app widgets with the remote adapter
		for (int i = 0; i < appWidgetIds.length; ++i) {

			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget);

			// enable to open browser activity on click to the big widget text
			Intent browserIntent = new Intent(context, BrowserActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
					browserIntent, 0);

			views.setOnClickPendingIntent(R.id.lbl_widget_big, pendingIntent);

			// set play functionality
			Intent playIntent = new Intent(context, WidgetService.class);
			playIntent.setAction(ACTION_PLAY);
			PendingIntent playPending = PendingIntent.getService(context, 0,
					playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			views.setOnClickPendingIntent(R.id.button_widget_play, playPending);

			// set stop functionality
			Intent stopIntent = new Intent(context, WidgetService.class);
			stopIntent.setAction(ACTION_STOP);
			PendingIntent stopPending = PendingIntent.getService(context, 0,
					stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			views.setOnClickPendingIntent(R.id.button_widget_quit, stopPending);

			// set next functionality
			Intent nextIntent = new Intent(context, WidgetService.class);
			nextIntent.setAction(ACTION_NEXT);
			PendingIntent nextPending = PendingIntent.getService(context, 0,
					nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			views.setOnClickPendingIntent(R.id.button_widget_next, nextPending);

			// set prev functionality
			Intent prevIntent = new Intent(context, WidgetService.class);
			prevIntent.setAction(ACTION_PREV);
			PendingIntent prevPending = PendingIntent.getService(context, 0,
					prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			views.setOnClickPendingIntent(R.id.button_widget_prev, prevPending);

			appWidgetManager.updateAppWidget(appWidgetIds[i], views);
		}
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
}
