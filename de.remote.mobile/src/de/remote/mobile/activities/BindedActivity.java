package de.remote.mobile.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;
import de.remote.mobile.R;
import de.remote.mobile.database.ServerDatabase;
import de.remote.mobile.services.PlayerBinder;
import de.remote.mobile.services.RemoteService;

/**
 * the activity handes connection with remote service.
 * 
 * @author sebastian
 * 
 */
public abstract class BindedActivity extends Activity {

	/**
	 * name for extra data for server name
	 */
	public static final String EXTRA_SERVER_ID = "serverId";

	/**
	 * binder object
	 */
	protected PlayerBinder binder;

	/**
	 * area that contains the download progress and cancel button
	 */
	protected LinearLayout downloadLayout;

	/**
	 * id of connected server
	 */
	protected int serverID = -1;

	/**
	 * database object
	 */
	protected ServerDatabase serverDB;

	/**
	 * connection to the service
	 */
	protected ServiceConnection playerConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (PlayerBinder) service;
			boolean newConnection = !binder.isConnected();
			disableScreen();
			binderConnected();
			// if there is a server in extra -> connect with this server
			if (getIntent().getExtras() != null
					&& getIntent().getExtras().containsKey(EXTRA_SERVER_ID)){
				serverID = getIntent().getExtras().getInt(EXTRA_SERVER_ID);
				newConnection = true;
			}
			// else just connect if there is no connection
			else if (newConnection) {
				serverID = serverDB.getFavoriteServer();
				if (serverID == -1)
					Toast.makeText(BindedActivity.this,
							"no server configurated", Toast.LENGTH_SHORT)
							.show();
			}
			// if there is a server id to connect -> connect
			if (serverID >= 0 && newConnection)
				binder.connectToServer(serverID, new Runnable() {
					@Override
					public void run() {
						remoteConnected();
					}
				});
			else if (!newConnection)
				remoteConnected();
		}
	};

	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// bind service
		Intent intent = new Intent(this, RemoteService.class);
		startService(intent);
		bindService(intent, playerConnection, Context.BIND_AUTO_CREATE);

		serverDB = new ServerDatabase(this);
	};
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.opt_server_select:
			Intent intent = new Intent(this, SelectServerActivity.class);
			startActivityForResult(intent, SelectServerActivity.RESULT_CODE);
			break;
		case R.id.opt_exit:
			binder.disconnect(null);
			intent = new Intent(this, RemoteService.class);
			stopService(intent);
			finish();
			break;			
		}
		return super.onMenuItemSelected(featureId, item);
	}

	/**
	 * perform on connection to the binder
	 */
	abstract void binderConnected();

	/**
	 * perform on connection to a remote system
	 */
	abstract void remoteConnected();

	/**
	 * disable the gui elements. inform user about connecting status.
	 */
	abstract void disableScreen();

	@Override
	protected void onDestroy() {
		serverDB.close();

		super.onDestroy();
	}
}
