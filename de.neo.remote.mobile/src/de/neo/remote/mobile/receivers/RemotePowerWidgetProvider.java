package de.neo.remote.mobile.receivers;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import de.neo.remote.mobile.activities.PowerActivity;
import de.neo.remote.mobile.services.WidgetPowerService;
import de.remote.mobile.R;

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

			setSwitchIntent(views, context, appWidgetIds[i]);

			appWidgetManager.updateAppWidget(appWidgetIds[i], views);
		}
	}

	public static void setSwitchIntent(RemoteViews views, Context context, int id) {
		// set switch functionality
		Intent switchIntent = new Intent(context, WidgetPowerService.class);
		switchIntent.setAction(ACTION_SWITCH);
		switchIntent.putExtra(PowerActivity.SWITCH_NUMBER, id);
		PendingIntent switchPending = PendingIntent.getService(context,
				switchIntent.hashCode(), switchIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		views.setOnClickPendingIntent(R.id.widget_power_layout, switchPending);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		onUpdate(context, manager, null);
	}

}
