package de.neo.remote.mobile.activities;

import java.util.List;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import de.neo.android.persistence.Dao;
import de.neo.android.persistence.DaoException;
import de.neo.android.persistence.DaoFactory;
import de.neo.remote.api.IInternetSwitch.State;
import de.neo.remote.api.GroundPlot;
import de.neo.remote.api.PlayingBean;
import de.neo.remote.mobile.persistence.RemoteDaoBuilder;
import de.neo.remote.mobile.persistence.RemoteServer;
import de.neo.remote.mobile.services.RemoteBinder;
import de.neo.remote.mobile.services.RemoteService;
import de.neo.remote.mobile.services.RemoteService.BufferdUnit;
import de.neo.remote.mobile.services.RemoteService.IRemoteActionListener;
import de.neo.remote.mobile.services.WidgetService;
import de.remote.mobile.R;

/**
 * the activity handes connection with remote service.
 * 
 * @author sebastian
 * 
 */
public abstract class AbstractConnectionActivity extends ActionBarActivity
		implements IRemoteActionListener {

	public static final String EXTRA_SERVER_ID = "server_id";

	public RemoteBinder mBinder;
	protected ProgressDialog mProgress;
	protected RemoteServer mCurrentServer;
	protected boolean mIsActive;

	protected ServiceConnection mPlayerConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBinder.removeRemoteActionListener(AbstractConnectionActivity.this);
			Log.e("disconnect service", "class: "
					+ AbstractConnectionActivity.this.getClass()
							.getSimpleName());
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBinder = (RemoteBinder) service;
			boolean needReconnect = !mBinder.isConnected();
			onRemoteBinder(mBinder);
			onStartConnecting();
			mBinder.addRemoteActionListener(AbstractConnectionActivity.this);
			Dao<RemoteServer> dao = DaoFactory.getInstance().getDao(
					RemoteServer.class);
			// if there is a server in extra -> connect with this server
			if (getIntent().getExtras() != null
					&& getIntent().getExtras().containsKey(EXTRA_SERVER_ID)) {
				try {
					mCurrentServer = dao.loadById(getIntent().getExtras()
							.getInt(EXTRA_SERVER_ID));
					needReconnect = true;
				} catch (DaoException e) {
					e.printStackTrace();
				}

			}
			// else just connect if there is no connection
			else if (needReconnect) {
				mCurrentServer = getFavoriteServer();
				if (mCurrentServer == null) {
					Toast.makeText(AbstractConnectionActivity.this,
							getString(R.string.server_no_favorite),
							Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(AbstractConnectionActivity.this,
							SelectServerActivity.class);
					startActivityForResult(intent,
							SelectServerActivity.RESULT_CODE);
				}
			}
			// if there is a server id to connect -> connect
			if (mCurrentServer != null && needReconnect)
				mBinder.connectToServer(mCurrentServer);
			else if (!needReconnect)
				AbstractConnectionActivity.this.onServerConnectionChanged(null);
		}

	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setLogo(R.drawable.ic_launcher);

		// bind remote service
		Intent intent = new Intent(this, RemoteService.class);
		startService(intent);
		bindService(intent, mPlayerConnection, Context.BIND_AUTO_CREATE);

		// start widget service
		intent = new Intent(this, WidgetService.class);
		startService(intent);

		DaoFactory.initiate(new RemoteDaoBuilder(this));
	}

	protected RemoteServer getFavoriteServer() {
		try {
			Dao<RemoteServer> dao = DaoFactory.getInstance().getDao(
					RemoteServer.class);
			List<RemoteServer> serverList = dao.loadAll();
			for (RemoteServer server : serverList) {
				if (server.isFavorite())
					return server;
			}
		} catch (DaoException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		mIsActive = true;
	}

	public boolean isActive() {
		return mIsActive;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SelectServerActivity.RESULT_CODE) {
			if (data == null || data.getExtras() == null)
				return;
			Dao<RemoteServer> dao = DaoFactory.getInstance().getDao(
					RemoteServer.class);
			try {
				mCurrentServer = dao.loadById(data.getExtras().getInt(
						SelectServerActivity.SERVER_ID));
				onStartConnecting();
				mBinder.connectToServer(mCurrentServer);
			} catch (DaoException e) {
				e.printStackTrace();
				mCurrentServer = null;
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.opt_server_select:
			Intent intent = new Intent(this, SelectServerActivity.class);
			startActivityForResult(intent, SelectServerActivity.RESULT_CODE);
			break;
		case R.id.opt_exit:
			// intent = new Intent(this, RemoteService.class);
			// stopService(intent);
			mBinder.disconnect();
			mBinder.removeRemoteActionListener(this);
			unbindService(mPlayerConnection);
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		dismissProgress();
		mIsActive = false;
		super.onPause();
	}

	public ProgressDialog createProgress() {
		dismissProgress();
		mProgress = new ProgressDialog(this);
		return mProgress;
	}

	public void dismissProgress() {
		if (mProgress != null)
			mProgress.dismiss();
		mProgress = null;
	}

	abstract void onRemoteBinder(RemoteBinder mBinder);

	/**
	 * start connection. inform user about connecting status.
	 */
	abstract void onStartConnecting();

	@Override
	protected void onDestroy() {
		DaoFactory.finilize();
		super.onDestroy();
	}

	@Override
	public void onPlayingBeanChanged(String mediaserver, PlayingBean bean) {
	}

	@Override
	public void onControlUnitCreated(BufferdUnit controlUnit) {
	}

	@Override
	public void onGroundPlotCreated(GroundPlot plot) {
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
	}

	@Override
	public void startSending(long size) {
	}

	@Override
	public void progressSending(long size) {
	}

	@Override
	public void endSending(long size) {
	}

	@Override
	public void sendingCanceled() {
	}

	public void progressFinished(Object result) {
	}

}
