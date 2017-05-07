package de.neo.smarthome.mobile.activities;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.ViewPager.LayoutParams;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import colorpickerview.dialog.ColorPickerDialogFragment.ColorPickerDialogListener;
import de.neo.android.opengl.AbstractSceneSurfaceView;
import de.neo.remote.web.JSONUtils;
import de.neo.smarthome.api.GroundPlot;
import de.neo.smarthome.api.IWebAction.BeanAction;
import de.neo.smarthome.api.IWebLEDStrip.BeanLEDStrips;
import de.neo.smarthome.api.IWebMediaServer.BeanMediaServer;
import de.neo.smarthome.api.IWebSwitch.BeanSwitch;
import de.neo.smarthome.mobile.persistence.RemoteServer;
import de.neo.smarthome.mobile.tasks.SimpleTask;
import de.neo.smarthome.mobile.tasks.SimpleTask.BackgroundAction;
import de.neo.smarthome.mobile.util.BeanClickHandler;
import de.neo.smarthome.mobile.util.ControlSceneRenderer;
import de.neo.smarthome.mobile.util.ParcelableWebBeans.ParcelBeanAction;
import de.neo.smarthome.mobile.util.ParcelableWebBeans.ParcelBeanLEDStrips;
import de.neo.smarthome.mobile.util.ParcelableWebBeans.ParcelBeanMediaServer;
import de.neo.smarthome.mobile.util.ParcelableWebBeans.ParcelBeanSwitch;
import de.remote.mobile.R;

public class ControlSceneActivity extends WebAPIActivity implements ColorPickerDialogListener {

	public static final String Beans = "beans";
	public static final String GroundPlotKey = "groundplot";

	private AbstractSceneSurfaceView mGLView;
	private ControlSceneRenderer mRenderer;
	private FrameLayout mLayout;
	private ProgressBar mProgress;
	private BeanClickHandler mSelecter;
	private ArrayList<Parcelable> mParcelableBeans = new ArrayList<>();
	private GroundPlot mGroundPlot;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSelecter = new BeanClickHandler(this, new Handler());

		setContentView(R.layout.controlscene);
		findComponents();

		mRenderer = new ControlSceneRenderer(this, mSelecter);
		mRenderer.setSwitchApi(mWebSwitch);
		mGLView = new AbstractSceneSurfaceView(this, savedInstanceState, mRenderer);

		LayoutParams params = new LayoutParams();
		params.height = LayoutParams.MATCH_PARENT;
		params.width = LayoutParams.MATCH_PARENT;
		mLayout.addView(mGLView, 0, params);
		loadControlCenter(savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mParcelableBeans != null && mParcelableBeans.size() > 0) {
			outState.putParcelableArrayList(Beans, mParcelableBeans);
		}
		if (mGroundPlot != null) {
			outState.putString(GroundPlotKey, JSONUtils.objectToJson(mGroundPlot).toString());
		}
	}

	private void loadControlCenter(Bundle saved) {
		AsyncTask<Integer, Object, Exception> task = new AsyncTask<Integer, Object, Exception>() {

			protected void onPreExecute() {
				setTitle(getResources().getString(R.string.connecting));
				mProgress.setVisibility(View.VISIBLE);
				mRenderer.clearControlCenter();
			};

			@Override
			protected void onProgressUpdate(Object... values) {
				if (values != null && values.length > 0) {
					Object o = values[0];
					if (o instanceof GroundPlot) {
						mGroundPlot = (GroundPlot) o;
						mRenderer.addGroundToScene(mGroundPlot);
					} else if (o instanceof List<?>) {
						for (Object web : (List<?>) o) {
							if (web instanceof BeanMediaServer) {
								BeanMediaServer ms = (BeanMediaServer) web;
								mRenderer.addMediaServer(ms);
								mParcelableBeans.add(new ParcelBeanMediaServer(ms));
							}
							if (web instanceof BeanSwitch) {
								BeanSwitch bs = (BeanSwitch) web;
								mRenderer.addSwitch(bs);
								mParcelableBeans.add(new ParcelBeanSwitch(bs));
							}
							if (web instanceof BeanAction) {
								BeanAction ba = (BeanAction) web;
								mRenderer.addAction(ba);
								mParcelableBeans.add(new ParcelBeanAction(ba));
							}
							if (web instanceof BeanLEDStrips) {
								BeanLEDStrips bs = (BeanLEDStrips) web;
								mRenderer.addLEDStrip(bs);
								mParcelableBeans.add(new ParcelBeanLEDStrips(bs));
							}
						}
					}
				}
			};

			@Override
			protected Exception doInBackground(Integer... params) {
				try {
					if (mWebControlCenter == null || mWebMediaServer == null || mWebLEDStrip == null
							|| mWebAction == null || mWebSwitch == null)
						throw new IllegalStateException(getString(R.string.no_controlcenter));
					publishProgress(mWebControlCenter.getGroundPlot());
					publishProgress(mWebMediaServer.getMediaServer(""));
					publishProgress(mWebLEDStrip.getLEDStrips());
					publishProgress(mWebAction.getActions());
					publishProgress(mWebSwitch.getSwitches());
				} catch (Exception e) {
					return e;
				}
				return null;
			}

			protected void onPostExecute(Exception result) {
				mProgress.setVisibility(View.GONE);
				if (result == null) {
					setTitle(getResources().getString(R.string.loaded_controlcenter));
					mRenderer.setConnectionState(true);
				} else {
					setTitle(getResources().getString(R.string.no_controlcenter));
					mRenderer.setConnectionState(false);
					showException(result);
				}
			};

		};
		if (saved != null && saved.containsKey(GroundPlotKey) && saved.containsKey(Beans)) {
			mParcelableBeans = saved.getParcelableArrayList(Beans);
			for (Parcelable bean : mParcelableBeans) {
				if (bean instanceof BeanMediaServer)
					mRenderer.addMediaServer((BeanMediaServer) bean);
				if (bean instanceof BeanSwitch)
					mRenderer.addSwitch((BeanSwitch) bean);
				if (bean instanceof BeanAction)
					mRenderer.addAction((BeanAction) bean);
				if (bean instanceof BeanLEDStrips)
					mRenderer.addLEDStrip((BeanLEDStrips) bean);
			}
			try {
				Object json = new JSONParser().parse(saved.getString(GroundPlotKey));
				mGroundPlot = (GroundPlot) JSONUtils.jsonToObject(GroundPlot.class, json, null, null);
				mRenderer.addGroundToScene(mGroundPlot);
				mProgress.setVisibility(View.GONE);
				setTitle(getResources().getString(R.string.loaded_controlcenter));
			} catch (ParseException | InstantiationException | IllegalAccessException e) {
				showException(e);
			}
		} else
			task.execute();
	}

	@Override
	protected void loadWebApi(RemoteServer server) {
		super.loadWebApi(server);
		if (mRenderer != null) {
			loadControlCenter(null);
			mRenderer.setSwitchApi(mWebSwitch);
		}
	}

	private void findComponents() {
		mLayout = (FrameLayout) findViewById(R.id.controlscene_layout);
		mProgress = (ProgressBar) findViewById(R.id.controlscene_progress);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mGLView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mGLView.onResume();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.opt_control_refresh:
			mCurrentServer = getFavoriteServer();
			loadWebApi(mCurrentServer);
			break;
		case R.id.opt_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());
		mi.inflate(R.menu.power_pref, menu);
		return true;
	}

	@Override
	public void onColorSelected(int dialogId, final int color) {
		if (mSelecter.getBean() instanceof BeanLEDStrips) {
			new SimpleTask(this).setSuccess("Color set")
					.setDialogMessage("Set color for " + mSelecter.getBean().getName() + " to " + color)
					.setDialogtitle("Set color...").setAction(new BackgroundAction() {

						@Override
						public void run() throws Exception {
							int blue = color & 0x0000FF;
							int green = (color & 0x00FF00) >> 8;
							int red = (color & 0xFF0000) >> 16;
							mWebLEDStrip.setColor(mSelecter.getBean().getID(), red, green, blue);
						}
					}).execute();
		}
	}

	@Override
	public void onDialogDismissed(int dialogId) {
		// TODO Auto-generated method stub

	}

}
