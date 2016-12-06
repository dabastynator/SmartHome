package de.neo.remote.mobile.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;
import de.neo.remote.api.IWebMediaServer;
import de.neo.remote.api.IWebMediaServer.BeanMediaServer;
import de.neo.remote.mobile.services.WidgetService;
import de.neo.remote.mobile.util.MediaServerAdapter;
import de.neo.rmi.protokol.RemoteException;
import de.remote.mobile.R;

public class SelectMediaServerActivity extends Activity {

	public static final String MS_NUMBER = "ms_number";

	private int appWidgetId;

	private SelectMSListener listener;
	protected ListView msList;

	private IWebMediaServer mWebMediaServer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mWebMediaServer = AbstractConnectionActivity.createWebMediaServer();

		setContentView(R.layout.mediaserver_list);

		findComponents();

		Bundle extras = getIntent().getExtras();
		appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		listener = new SelectMSListener();

		new AsyncTask<Integer, Integer, Void>() {

			ArrayList<BeanMediaServer> mediaserver;
			String[] ids;

			@Override
			protected Void doInBackground(Integer... params) {
				try {
					mediaserver = mWebMediaServer.getMediaServer("");
					ids = new String[mediaserver.size()];
					for (int i = 0; i < mediaserver.size(); i++)
						ids[i] = mediaserver.get(i).getID();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;

			}

			@Override
			protected void onPostExecute(Void result) {
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
			SharedPreferences prefs = SelectMediaServerActivity.this.getSharedPreferences(WidgetService.PREFERENCES, 0);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putString("" + appWidgetId, mediaserver.getID());
			edit.commit();

			WidgetService.updateMusicWidget(getApplicationContext(), appWidgetId, mediaserver);

			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			setResult(RESULT_OK, resultValue);
			finish();
			return false;
		}

	}

}
