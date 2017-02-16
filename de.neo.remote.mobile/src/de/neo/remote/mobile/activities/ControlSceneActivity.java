package de.neo.remote.mobile.activities;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager.LayoutParams;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import colorpickerview.dialog.ColorPickerDialogFragment;
import colorpickerview.dialog.ColorPickerDialogFragment.ColorPickerDialogListener;
import de.neo.android.opengl.AbstractSceneSurfaceView;
import de.neo.remote.api.GroundPlot;
import de.neo.remote.api.IControlCenter.BeanWeb;
import de.neo.remote.api.IWebAction.BeanAction;
import de.neo.remote.api.IWebLEDStrip.BeanLEDStrips;
import de.neo.remote.api.IWebMediaServer.BeanMediaServer;
import de.neo.remote.api.IWebSwitch.BeanSwitch;
import de.neo.remote.mobile.persistence.RemoteServer;
import de.neo.remote.mobile.tasks.SimpleTask;
import de.neo.remote.mobile.tasks.SimpleTask.BackgroundAction;
import de.neo.remote.mobile.util.ControlSceneRenderer;
import de.remote.mobile.R;

public class ControlSceneActivity extends WebAPIActivity implements ColorPickerDialogListener {

	private AbstractSceneSurfaceView mGLView;
	private ControlSceneRenderer mRenderer;
	private Handler mHandler;
	private FrameLayout mLayout;
	private ProgressBar mProgress;
	private SelectControlUnit mSelecter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mHandler = new Handler();

		mSelecter = new SelectControlUnit();

		setContentView(R.layout.controlscene);
		findComponents();

		mRenderer = new ControlSceneRenderer(this, mSelecter);
		mRenderer.setSwitchApi(mWebSwitch);
		mGLView = new AbstractSceneSurfaceView(this, savedInstanceState, mRenderer);

		LayoutParams params = new LayoutParams();
		params.height = LayoutParams.MATCH_PARENT;
		params.width = LayoutParams.MATCH_PARENT;
		mLayout.addView(mGLView, 0, params);
		loadControlCenter();
	}

	private void loadControlCenter() {
		new AsyncTask<Integer, Object, Exception>() {

			protected void onPreExecute() {
				setTitle(getResources().getString(R.string.connecting));
				mProgress.setVisibility(View.VISIBLE);
				mRenderer.clearControlCenter();
			};

			@Override
			protected void onProgressUpdate(Object... values) {
				if (values != null && values.length > 0) {
					Object o = values[0];
					if (o instanceof GroundPlot)
						mRenderer.addGroundToScene((GroundPlot) o);
					else if (o instanceof List<?>) {
						for (Object web : (List<?>) o) {
							if (web instanceof BeanMediaServer)
								mRenderer.addMediaServer((BeanMediaServer) web);
							if (web instanceof BeanSwitch)
								mRenderer.addSwitch((BeanSwitch) web);
							if (web instanceof BeanAction)
								mRenderer.addAction((BeanAction) web);
							if (web instanceof BeanLEDStrips)
								mRenderer.addLEDStrip((BeanLEDStrips) web);
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

		}.execute();
	}

	@Override
	protected void loadWebApi(RemoteServer server) {
		super.loadWebApi(server);
		if (mRenderer != null) {
			loadControlCenter();
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

	public class SelectControlUnit implements OnClickListener {
		private BeanWeb mBean;

		public void selectBean(final BeanWeb bean) {
			mBean = bean;
			if (mBean instanceof BeanMediaServer) {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						Intent intent = new Intent(ControlSceneActivity.this, MediaServerActivity.class);
						intent.putExtra(MediaServerActivity.EXTRA_MEDIA_ID, mBean.getID());
						ControlSceneActivity.this.startActivity(intent);
					}
				});
			}
			if (mBean instanceof BeanAction) {
				AsyncTask<BeanWeb, Integer, Exception> task = new AsyncTask<BeanWeb, Integer, Exception>() {

					@Override
					protected Exception doInBackground(BeanWeb... params) {
						try {
							BeanAction action = (BeanAction) mBean;
							mWebAction.startAction(action.getID());
						} catch (Exception e) {
							return e;
						}
						return null;
					}

					@Override
					protected void onPostExecute(Exception result) {
						BeanAction action = (BeanAction) mBean;
						if (result != null)
							showException(result);
						else {
							Toast.makeText(getApplicationContext(), "Execute: " + action.getDescription(),
									Toast.LENGTH_SHORT).show();
						}
						if (action.getClientAction() != null && action.getClientAction().length() > 0) {
							Uri uri = Uri.parse(action.getClientAction());
							Intent intent = new Intent(Intent.ACTION_VIEW, uri);
							startActivity(intent);

						}
					}
				};
				task.execute(mBean);
			}

			if (mBean instanceof BeanLEDStrips) {
				BeanLEDStrips led = (BeanLEDStrips) mBean;
				ColorPickerDialogFragment dialog = ColorPickerDialogFragment.newInstance(led.getID().hashCode(),
						"Pick color for " + led.getName(), getString(android.R.string.ok), led.getColor() | 0xFF000000,
						false);
				dialog.show(getFragmentManager(), "colorpicker");
			}
		}

		public void selectLongClickUnit(final BeanWeb bean) {
			mBean = bean;
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					new AlertDialog.Builder(ControlSceneActivity.this)
							.setTitle(getResources().getString(R.string.command_stop))
							.setMessage(
									getResources().getString(R.string.command_stop_long) + " " + mBean.getName() + "?")
							.setPositiveButton(getResources().getString(android.R.string.yes), SelectControlUnit.this)
							.setNegativeButton(getResources().getString(android.R.string.no), null).create().show();
				}
			});
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (mBean instanceof BeanAction) {
				AsyncTask<BeanWeb, Integer, Exception> task = new AsyncTask<BeanWeb, Integer, Exception>() {

					@Override
					protected Exception doInBackground(BeanWeb... params) {
						try {
							BeanAction action = (BeanAction) mBean;
							mWebAction.stopAction(action.getID());
						} catch (Exception e) {
							return e;
						}
						return null;
					}

					@Override
					protected void onPostExecute(Exception result) {
						if (result != null)
							showException(result);
						else {
							Toast.makeText(getApplicationContext(), "Stop: " + mBean.getDescription(),
									Toast.LENGTH_SHORT).show();
						}
					}
				};
				task.execute(mBean);
			}
		}
	}

	@Override
	public void onColorSelected(int dialogId, final int color) {
		if (mSelecter.mBean instanceof BeanLEDStrips) {
			new SimpleTask(this).setSuccess("Color set")
					.setDialogMessage("Set color for " + mSelecter.mBean.getName() + " to " + color)
					.setDialogtitle("Set color...").setAction(new BackgroundAction() {

						@Override
						public void run() throws Exception {
							int blue = color & 0x0000FF;
							int green = (color & 0x00FF00) >> 8;
							int red = (color & 0xFF0000) >> 16;
							mWebLEDStrip.setColor(mSelecter.mBean.getID(), red, green, blue);
						}
					}).execute();
		}
	}

	@Override
	public void onDialogDismissed(int dialogId) {
		// TODO Auto-generated method stub

	}

}
