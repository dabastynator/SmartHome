package de.neo.remote.mobile.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;
import de.neo.remote.gpiopower.api.IInternetSwitch.State;
import de.neo.remote.mediaserver.api.PlayingBean;
import de.neo.remote.mobile.database.RemoteDatabase;
import de.neo.remote.mobile.services.PlayerBinder;
import de.neo.remote.mobile.services.RemoteService;
import de.neo.remote.mobile.services.RemoteService.IRemoteActionListener;
import de.remote.mobile.R;

/**
 * the activity handes connection with remote service.
 * 
 * @author sebastian
 * 
 */
public abstract class BindedActivity extends Activity implements
		IRemoteActionListener {

	/**
	 * name for extra data for server name
	 */
	public static final String EXTRA_SERVER_ID = "serverId";

	/**
	 * binder object
	 */
	public PlayerBinder binder;

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
	protected RemoteDatabase serverDB;

	/**
	 * handler to post runnables on the gui thread
	 */
	protected Handler handler = new Handler();

	/**
	 * connection to the service
	 */
	protected ServiceConnection playerConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			binder.removeRemoteActionListener(BindedActivity.this);
			Log.e("disconnect service", "lass: " + BindedActivity.this.getClass().getSimpleName());
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (PlayerBinder) service;
			boolean newConnection = !binder.isConnected();
			onStartConnecting();
			onBinderConnected();
			binder.addRemoteActionListener(BindedActivity.this);
			// if there is a server in extra -> connect with this server
			if (getIntent().getExtras() != null
					&& getIntent().getExtras().containsKey(EXTRA_SERVER_ID)) {
				serverID = getIntent().getExtras().getInt(EXTRA_SERVER_ID);
				newConnection = true;
			}
			// else just connect if there is no connection
			else if (newConnection) {
				serverID = serverDB.getServerDao().getFavoriteServer();
				if (serverID == -1) {
					Toast.makeText(BindedActivity.this, "no favorite server",
							Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(BindedActivity.this,
							SelectServerActivity.class);
					startActivityForResult(intent,
							SelectServerActivity.RESULT_CODE);
				}
			}
			// if there is a server id to connect -> connect
			if (serverID >= 0 && newConnection)
				binder.connectToServer(serverID);
			else if (!newConnection)
				BindedActivity.this.onServerConnectionChanged(null, -1);
		}
		
	};

	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// bind service
		Intent intent = new Intent(this, RemoteService.class);
		startService(intent);
		bindService(intent, playerConnection, Context.BIND_AUTO_CREATE);

		serverDB = new RemoteDatabase(this);
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SelectServerActivity.RESULT_CODE) {
			if (data == null || data.getExtras() == null)
				return;
			serverID = data.getExtras().getInt(SelectServerActivity.SERVER_ID);
			onStartConnecting();
			binder.connectToServer(serverID);
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.opt_server_select:
			Intent intent = new Intent(this, SelectServerActivity.class);
			startActivityForResult(intent, SelectServerActivity.RESULT_CODE);
			break;
		case R.id.opt_exit:
			// intent = new Intent(this, RemoteService.class);
			// stopService(intent);
			binder.disconnect();
			binder.removeRemoteActionListener(this);
			unbindService(playerConnection);
			finish();
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	/**
	 * perform on connection to the binder
	 */
	abstract void onBinderConnected();

	/**
	 * start connection. inform user about connecting status.
	 */
	abstract void onStartConnecting();

	@Override
	protected void onDestroy() {
		serverDB.close();
		
		super.onDestroy();
	}

	@Override
	public void onPlayingBeanChanged(String mediaserver, PlayingBean bean) {
	}

	@Override
	public void onStopService() {
	}

	@Override
	public void startReceive(long size, String file) {
	}

	@Override
	public void progressReceive(long size, String file) {
	}

	@Override
	public void endReceive(long size) {
	}

	@Override
	public void exceptionOccurred(Exception e) {
	}

	@Override
	public void downloadCanceled() {
	}
	
	@Override
	public void onPowerSwitchChange(String _switch, State state) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startSending(long size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void progressSending(long size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endSending(long size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendingCanceled() {
		// TODO Auto-generated method stub
		
	}
}
