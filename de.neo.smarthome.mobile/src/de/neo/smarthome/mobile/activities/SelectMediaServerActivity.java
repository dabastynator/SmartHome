package de.neo.smarthome.mobile.activities;

import java.util.ArrayList;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;
import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.api.IWebMediaServer.BeanMediaServer;
import de.neo.smarthome.mobile.services.RemoteService;
import de.neo.smarthome.mobile.util.MediaServerAdapter;
import de.neo.smarthome.mobile.util.WidgetUpdater;
import de.remote.mobile.R;

public class SelectMediaServerActivity extends WebAPIActivity {

	public static final String MS_NUMBER = "ms_number";

	private int appWidgetId;

	private SelectMSListener listener;
	protected ListView msList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.mediaserver_list);

		findComponents();

		Bundle extras = getIntent().getExtras();
		appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		listener = new SelectMSListener();

		new AsyncTask<Integer, Integer, Exception>() {

			ArrayList<BeanMediaServer> mediaserver;
			String[] ids;

			@Override
			protected Exception doInBackground(Integer... params) {
				try {
					mediaserver = mWebMediaServer.getMediaServer("");
					ids = new String[mediaserver.size()];
					for (int i = 0; i < mediaserver.size(); i++)
						ids[i] = mediaserver.get(i).getID();
				} catch (RemoteException e) {
					return e;
				}
				return null;

			}

			@Override
			protected void onPostExecute(Exception result) {
				if (result != null)
					showException(result);
				msList.setAdapter(new MediaServerAdapter(getApplicationContext(), mediaserver, ids, listener));
			}
		}.execute();

	}

	private void findComponents() {
		msList = (ListView) findViewById(R.id.list_mediaserver);
	}

	public class SelectMSListener {

		public boolean onSelectSwitch(BeanMediaServer mediaserver) {
			// Store selection
			SharedPreferences prefs = SelectMediaServerActivity.this.getSharedPreferences(RemoteService.PREFERENCES, 0);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putString("" + appWidgetId, mediaserver.getID());
			edit.commit();

			WidgetUpdater updater = new WidgetUpdater(getApplicationContext());
			updater.updateMusicWidget(appWidgetId, mediaserver);

			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			setResult(RESULT_OK, resultValue);
			finish();
			return false;
		}

	}

}
