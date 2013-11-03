package de.neo.remote.mobile.receivers;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import de.neo.remote.mobile.activities.BrowserActivity;
import de.neo.remote.mobile.services.WidgetService;
import de.remote.mobile.R;

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
		// appWidgetIds contains not all necessary ids -> get all ids
		Intent serviceIntent = new Intent(context, WidgetService.class);
		context.startService(serviceIntent);
		ComponentName thisWidget = new ComponentName(context,
				RemoteWidgetProvider.class);
		appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		
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

			// Register update onClickListener
			Intent intent = new Intent(context, RemoteWidgetProvider.class);

			intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

			pendingIntent = PendingIntent.getBroadcast(context,
					0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

			appWidgetManager.updateAppWidget(appWidgetIds[i], views);
		}
	}
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		onUpdate(context, manager, null);
	}
	
}
