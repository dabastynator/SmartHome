package de.remote.mobile.receivers;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import de.remote.mobile.R;
import de.remote.mobile.services.WidgetPowerService;

/**
 * the remote widget provider starts the widget service.
 * 
 * @author sebastian
 */
public class RemotePowerWidgetProvider extends AppWidgetProvider {

	/**
	 * click to make power on
	 */
	public static final String ACTION_SWITCH = "de.remote.power.SWITCH";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// appWidgetIds contains not all necessary ids -> get all ids
		Intent serviceIntent = new Intent(context, WidgetPowerService.class);
		context.startService(serviceIntent);
		ComponentName thisWidget = new ComponentName(context,
				RemotePowerWidgetProvider.class);
		appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		
		// update each of the app widgets with the remote adapter
		for (int i = 0; i < appWidgetIds.length; ++i) {

			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.power_widget);

			// set switch functionality
			Intent playIntent = new Intent(context, WidgetPowerService.class);
			playIntent.setAction(ACTION_SWITCH);
			PendingIntent playPending = PendingIntent.getService(context, 0,
					playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			views.setOnClickPendingIntent(R.id.image_power_widget, playPending);

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
