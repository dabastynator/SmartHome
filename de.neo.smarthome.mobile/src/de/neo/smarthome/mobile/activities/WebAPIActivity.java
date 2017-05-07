package de.neo.smarthome.mobile.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import de.neo.android.persistence.Dao;
import de.neo.android.persistence.DaoException;
import de.neo.android.persistence.DaoFactory;
import de.neo.remote.web.WebProxyBuilder;
import de.neo.smarthome.api.IControlCenter;
import de.neo.smarthome.api.IWebAction;
import de.neo.smarthome.api.IWebLEDStrip;
import de.neo.smarthome.api.IWebMediaServer;
import de.neo.smarthome.api.IWebSwitch;
import de.neo.smarthome.mobile.persistence.RemoteDaoBuilder;
import de.neo.smarthome.mobile.persistence.RemoteServer;
import de.neo.smarthome.mobile.services.RemoteService;
import de.neo.smarthome.mobile.tasks.AbstractTask;
import de.neo.smarthome.mobile.util.ExceptionHandler;
import de.remote.mobile.R;

/**
 * the activity handes connection with remote service.
 * 
 * @author sebastian
 * 
 */
public class WebAPIActivity extends ActionBarActivity {

	public static final String EXTRA_SERVER_ID = "server_id";

	protected ProgressDialog mProgressDialog;
	protected List<AlertDialog> mDialogs = new ArrayList<>();
	protected RemoteServer mCurrentServer;
	protected boolean mIsActive;

	protected IWebSwitch mWebSwitch;
	protected IWebLEDStrip mWebLEDStrip;
	protected IWebAction mWebAction;
	protected IWebMediaServer mWebMediaServer;
	protected IControlCenter mWebControlCenter;

	private ExceptionHandler mHandleAppCrash;

	protected DaoFactory mDaoFactory;

	protected void onCreate(Bundle savedInstanceState) {
		mHandleAppCrash = new ExceptionHandler(this);
		Thread.setDefaultUncaughtExceptionHandler(mHandleAppCrash);
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setLogo(R.drawable.ic_launcher);

		// start widget service
		Intent intent = new Intent(this, RemoteService.class);
		startService(intent);

		DaoFactory.initiate(new RemoteDaoBuilder(this));
		mDaoFactory = DaoFactory.getInstance();
		mCurrentServer = getFavoriteServer();
		if (mCurrentServer == null) {
			intent = new Intent(this, SelectServerActivity.class);
			startActivityForResult(intent, SelectServerActivity.RESULT_CODE);
		}

		loadWebApi(mCurrentServer);
	}

	protected void loadWebApi(RemoteServer server) {
		if (server != null) {
			WebProxyBuilder b = new WebProxyBuilder().setSecurityToken(server.getApiToken());
			String url = server.getEndPoint();
			mWebSwitch = b.setEndPoint(url + "/switch").setInterface(IWebSwitch.class).create();
			mWebLEDStrip = b.setEndPoint(url + "/ledstrip").setInterface(IWebLEDStrip.class).create();
			mWebMediaServer = b.setEndPoint(url + "/mediaserver").setInterface(IWebMediaServer.class).create();
			mWebAction = b.setEndPoint(url + "/action").setInterface(IWebAction.class).create();
			mWebControlCenter = b.setEndPoint(url + "/controlcenter").setInterface(IControlCenter.class).create();
		}
	}

	public RemoteServer getFavoriteServer() {
		try {
			Dao<RemoteServer> dao = mDaoFactory.getDao(RemoteServer.class);
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

	protected void addDialog(AlertDialog dialog) {
		mDialogs.add(dialog);
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
			Dao<RemoteServer> dao = mDaoFactory.getDao(RemoteServer.class);
			try {
				mCurrentServer = dao.loadById(data.getExtras().getInt(SelectServerActivity.SERVER_ID));
				loadWebApi(mCurrentServer);
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
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		dismissProgress();
		dismissDialogs();
		mIsActive = false;
		super.onPause();
	}

	public ProgressDialog createProgress() {
		dismissProgress();
		mProgressDialog = new ProgressDialog(this);
		return mProgressDialog;
	}

	public void dismissProgress() {
		if (mProgressDialog != null)
			mProgressDialog.dismiss();
		mProgressDialog = null;
	}

	public void showException(Exception e) {
		dismissDialogs();
		AlertDialog dialog = new AbstractTask.ErrorDialog(this, e).show();
		mDialogs.add(dialog);
		dialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				mDialogs.remove(dialog);
			}
		});
	}

	public void dismissDialogs() {
		for (AlertDialog d : mDialogs)
			d.dismiss();
		mDialogs.clear();
	}

	@Override
	protected void onDestroy() {
		DaoFactory.finilize();
		super.onDestroy();
	}

	public IWebAction getWebAction() {
		return mWebAction;
	}

	public IWebLEDStrip getWebLED() {
		return mWebLEDStrip;
	}

}
