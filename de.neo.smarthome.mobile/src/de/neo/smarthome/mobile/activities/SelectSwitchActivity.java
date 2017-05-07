package de.neo.smarthome.mobile.activities;

import java.util.ArrayList;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;
import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.api.IWebSwitch.BeanSwitch;
import de.neo.smarthome.mobile.services.RemoteService;
import de.neo.smarthome.mobile.tasks.AbstractTask;
import de.neo.smarthome.mobile.util.SwitchAdapter;
import de.neo.smarthome.mobile.util.WidgetUpdater;
import de.remote.mobile.R;

public class SelectSwitchActivity extends WebAPIActivity {

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

		new AsyncTask<Integer, Integer, Exception>() {

			ArrayList<BeanSwitch> switches = new ArrayList<>();
			String[] ids = {};

			@Override
			protected Exception doInBackground(Integer... params) {
				try {
					switches = mWebSwitch.getSwitches();
					ids = new String[switches.size()];
					for (int i = 0; i < switches.size(); i++)
						ids[i] = switches.get(i).getID();
				} catch (RemoteException e) {
					return e;
				}
				return null;

			}

			@Override
			protected void onPostExecute(Exception result) {
				if (switches != null && ids != null)
					switchList.setAdapter(new SwitchAdapter(SelectSwitchActivity.this, switches, ids, listener));
				if (result != null)
					new AbstractTask.ErrorDialog(SelectSwitchActivity.this, result).show();
			}
		}.execute();

	}

	private void findComponents() {
		switchList = (ListView) findViewById(R.id.list_switches);
	}

	public class SelectSwitchListener {

		public boolean onSelectSwitch(BeanSwitch webSwitch) {
			// Speichere Auswahl
			SharedPreferences prefs = SelectSwitchActivity.this.getSharedPreferences(RemoteService.PREFERENCES, 0);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putString("" + appWidgetId, webSwitch.getID());
			edit.commit();

			WidgetUpdater updater = new WidgetUpdater(getApplicationContext());
			updater.updateSwitchWidget(appWidgetId, webSwitch);

			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			setResult(RESULT_OK, resultValue);
			finish();
			return false;
		}

	}

}
