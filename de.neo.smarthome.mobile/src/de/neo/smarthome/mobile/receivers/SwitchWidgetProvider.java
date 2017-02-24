package de.neo.smarthome.mobile.receivers;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import de.neo.smarthome.mobile.services.RemoteService;

/**
 * the remote widget provider starts the widget service.
 * 
 * @author sebastian
 */
public class SwitchWidgetProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Intent serviceIntent = new Intent(context, RemoteService.class);
		serviceIntent.setAction(RemoteService.ACTION_UPDATE);
		context.startService(serviceIntent);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		onUpdate(context, manager, null);
	}

}
