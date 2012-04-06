package de.remote.mobile.receivers;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
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
	public static final String ACTION_PLAY = "actionPlay";

	/**
	 * click on stop action
	 */
	public static final String ACTION_STOP = "actionStop";

	/**
	 * click on next action
	 */
	public static final String ACTION_NEXT = "actionNext";

	/**
	 * click on prev action
	 */
	public static final String ACTION_PREV = "actionPrev";

	/**
	 * click on update action
	 */
	public static final String ACTION_UPDATE = "actionUpdate";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		Intent intent = new Intent(context.getApplicationContext(),
				WidgetService.class);
		// Update the widgets via the service
		context.startService(intent);
	}

}
