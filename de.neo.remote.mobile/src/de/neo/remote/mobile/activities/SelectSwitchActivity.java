package de.neo.remote.mobile.activities;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.RemoteViews;
import de.neo.remote.api.IWebSwitch.BeanSwitch;
import de.neo.remote.mobile.persistence.RemoteServer;
import de.neo.remote.mobile.receivers.RemotePowerWidgetProvider;
import de.neo.remote.mobile.services.WidgetService;
import de.neo.remote.mobile.util.SwitchAdapter;
import de.neo.rmi.protokol.RemoteException;
import de.remote.mobile.R;

public class SelectSwitchActivity extends AbstractConnectionActivity {

	public static final String SWITCH_NUMBER = "switch_number";
	public static final String WIDGET_PREFS = "prefs";

	private int appWidgetId;

	private SelectSwitchListener listener;
	protected ListView switchList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.switch_main);

		findComponents();

		Bundle extras = getIntent().getExtras();
		appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		listener = new SelectSwitchListener();

		new AsyncTask<Integer, Integer, Void>() {

			ArrayList<BeanSwitch> switches;
			String[] ids;

			@Override
			protected Void doInBackground(Integer... params) {
				try {
					switches = mWebSwitch.getSwitches();
					ids = new String[switches.size()];
					for (int i = 0; i < switches.size(); i++)
						ids[i] = switches.get(i).getID();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;

			}

			@Override
			protected void onPostExecute(Void result) {
				switchList.setAdapter(new SwitchAdapter(getApplicationContext(), switches, ids, listener));
			}
		}.execute();

	}

	private void findComponents() {
		switchList = (ListView) findViewById(R.id.list_switches);
	}

	@Override
	public void onServerConnectionChanged(RemoteServer server) {
	}

	public class SelectSwitchListener {

		public boolean onSelectSwitch(String switchID, String switchName) {
			// Speichere Auswahl
			SharedPreferences prefs = SelectSwitchActivity.this.getSharedPreferences(WIDGET_PREFS, 0);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putString("" + appWidgetId, switchID);
			edit.commit();

			AppWidgetManager manager = AppWidgetManager.getInstance(SelectSwitchActivity.this);
			RemoteViews views = new RemoteViews(SelectSwitchActivity.this.getPackageName(), R.layout.switch_widget);
			Intent switchIntent = new Intent(SelectSwitchActivity.this, WidgetService.class);
			switchIntent.setAction(RemotePowerWidgetProvider.ACTION_SWITCH);
			switchIntent.putExtra(SWITCH_NUMBER, appWidgetId);
			switchIntent.setData(Uri.withAppendedPath(Uri.parse("ABCD://widget/id/"), String.valueOf(appWidgetId)));
			PendingIntent switchPending = PendingIntent.getService(SelectSwitchActivity.this, appWidgetId, switchIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			views.setOnClickPendingIntent(R.id.widget_power_layout, switchPending);
			views.setImageViewResource(R.id.image_power_widget, R.drawable.light_off);
			views.setTextViewText(R.id.text_power_widget, switchName);
			manager.updateAppWidget(appWidgetId, views);

			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			setResult(RESULT_OK, resultValue);
			finish();
			return false;
		}

	}

}
