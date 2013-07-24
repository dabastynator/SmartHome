package de.remote.mobile.activities;

import java.util.Map;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RemoteViews;
import de.remote.gpiopower.api.IInternetSwitch;
import de.remote.mobile.R;
import de.remote.mobile.receivers.RemotePowerWidgetProvider;
import de.remote.mobile.services.WidgetPowerService;
import de.remote.mobile.util.SwitchAdapter;

public class SelectSwitchActivity extends PowerActivity {

	public static final String WIDGET_PREFS = "prefs";

	private int appWidgetId;

	private SelectSwitchListener listener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);
		listener = new SelectSwitchListener();
	}

	@Override
	public void onServerConnectionChanged(String serverName, int serverID) {
		if (binder.isConnected()) {
			Map<String, IInternetSwitch> power = getPower(binder);
			String[] switches = power.keySet().toArray(new String[power.size()]);
			switchList.setAdapter(new SwitchAdapter(this, switches, power,
					listener));
		} else {
			switchList
					.setAdapter(new SwitchAdapter(this, new String[] {}, null));
		}
	}

	public class SelectSwitchListener {

		public boolean onSelectSwitch(String switchName) {
			// Speichere Auswahl
			SharedPreferences prefs = SelectSwitchActivity.this
					.getSharedPreferences(WIDGET_PREFS, 0);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putString("" + appWidgetId, switchName);
			edit.commit();

			AppWidgetManager manager = AppWidgetManager
					.getInstance(SelectSwitchActivity.this);
			RemoteViews views = new RemoteViews(
					SelectSwitchActivity.this.getPackageName(),
					R.layout.power_widget);
			Intent switchIntent = new Intent(SelectSwitchActivity.this,
					WidgetPowerService.class);
			switchIntent.setAction(RemotePowerWidgetProvider.ACTION_SWITCH);
			switchIntent.putExtra(PowerActivity.SWITCH_NUMBER, appWidgetId);
			PendingIntent switchPending = PendingIntent.getService(
					SelectSwitchActivity.this, switchIntent.hashCode(),
					switchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			views.setOnClickPendingIntent(R.id.widget_power_layout,
					switchPending);
			views.setImageViewResource(R.id.image_power_widget,
					R.drawable.light_off);
			views.setTextViewText(R.id.text_power_widget, switchName);
			manager.updateAppWidget(appWidgetId, views);

			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					appWidgetId);
			setResult(RESULT_OK, resultValue);
			finish();
			return false;
		}

	}

}
