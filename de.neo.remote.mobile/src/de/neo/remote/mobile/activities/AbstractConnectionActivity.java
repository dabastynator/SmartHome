package de.neo.remote.mobile.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import de.neo.remote.api.IInternetSwitch.State;
import de.neo.remote.api.PlayingBean;
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
public abstract class AbstractConnectionActivity extends Activity implements
		IRemoteActionListener {

	/**
	 * name for extra data for server name
	 */
	public static final String EXTRA_SERVER_ID = "serverId";

	/**
	 * binder object
	 */
	public PlayerBinder binder;

	protected ProgressDialog progress;

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
			binder.removeRemoteActionListener(AbstractConnectionActivity.this);
			Log.e("disconnect service", "lass: "
					+ AbstractConnectionActivity.this.getClass()
							.getSimpleName());
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (PlayerBinder) service;
			boolean newConnection = !binder.isConnected();
			onStartConnecting();
			onBinderConnected();
			binder.addRemoteActionListener(AbstractConnectionActivity.this);
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
					Toast.makeText(
							AbstractConnectionActivity.this,
							getResources().getString(
									R.string.str_no_favorite_server),
							Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(AbstractConnectionActivity.this,
							SelectServerActivity.class);
					startActivityForResult(intent,
							SelectServerActivity.RESULT_CODE);
				}
			}
			// if there is a server id to connect -> connect
			if (serverID >= 0 && newConnection)
				binder.connectToServer(serverID);
			else if (!newConnection)
				AbstractConnectionActivity.this.onServerConnectionChanged(null,
						-1);
		}

	};

	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// bind remote service
		Intent intent = new Intent(this, RemoteService.class);
		startService(intent);
		bindService(intent, playerConnection, Context.BIND_AUTO_CREATE);

		// start widget service
		intent = new Intent(this, RemoteService.class);
		startService(intent);

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

	@Override
	protected void onPause() {
		dismissProgress();
		super.onPause();
	}

	public ProgressDialog createProgress() {
		dismissProgress();
		progress = new ProgressDialog(this);
		return progress;
	}

	public void dismissProgress() {
		if (progress != null)
			progress.dismiss();
		progress = null;
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

	public void progressFinished(Object result) {
		// TODO Auto-generated method stub

	}

}
