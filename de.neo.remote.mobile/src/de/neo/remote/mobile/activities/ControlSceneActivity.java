package de.neo.remote.mobile.activities;

import java.util.Collection;
import java.util.Set;

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
import de.neo.remote.api.ICommandAction;
import de.neo.remote.api.IMediaServer;
import de.neo.remote.api.IRCColor;
import de.neo.remote.api.IWebSwitch.State;
import de.neo.remote.api.PlayingBean;
import de.neo.remote.mobile.persistence.RemoteServer;
import de.neo.remote.mobile.services.RemoteBinder;
import de.neo.remote.mobile.services.RemoteService.BufferdUnit;
import de.neo.remote.mobile.tasks.AbstractTask;
import de.neo.remote.mobile.tasks.SimpleTask;
import de.neo.remote.mobile.tasks.SimpleTask.BackgroundAction;
import de.neo.remote.mobile.util.ControlSceneRenderer;
import de.remote.mobile.R;

public class ControlSceneActivity extends AbstractConnectionActivity implements ColorPickerDialogListener {

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
		mGLView = new AbstractSceneSurfaceView(this, savedInstanceState, mRenderer);

		LayoutParams params = new LayoutParams();
		params.height = LayoutParams.MATCH_PARENT;
		params.width = LayoutParams.MATCH_PARENT;
		mLayout.addView(mGLView, 0, params);

		setTitle(getResources().getString(R.string.connecting));
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
	public void onServerConnectionChanged(RemoteServer server) {
		mRenderer.setConnections(mBinder.isConnected());
		if (mBinder.getControlCenter() == null) {
			setTitle(getResources().getString(R.string.no_controlcenter));
			mProgress.setVisibility(View.GONE);
		} else {
			new UpdateGLViewTask(this).execute();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.opt_control_refresh:
			mRenderer.clearControlCenter();
			setTitle(getResources().getString(R.string.refresh));
			mProgress.setVisibility(View.VISIBLE);
			RemoteServer server = mBinder.getServer();
			if (server == null)
				server = getFavoriteServer();
			mBinder.connectToServer(server, this);
			break;
		case R.id.opt_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPlayingBeanChanged(String mediaserver, PlayingBean bean) {
		mRenderer.setPlayingBean(mediaserver, bean);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());
		mi.inflate(R.menu.power_pref, menu);
		return true;
	}

	@Override
	public void onPowerSwitchChange(String _switch, State state) {
		mRenderer.powerSwitchChanged(_switch, state);

	}

	@Override
	void onRemoteBinder(RemoteBinder mBinder) {
		// TODO Auto-generated method stub

	}

	@Override
	void onStartConnecting() {
		setTitle(getResources().getString(R.string.connecting));
		mProgress.setVisibility(View.VISIBLE);
	}

	@Override
	public void onControlUnitCreated(BufferdUnit controlUnit) {
		mRenderer.addControlUnitToScene(controlUnit);
	}

	@Override
	public void onGroundPlotCreated(GroundPlot plot) {
		mRenderer.addGroundToScene(plot);
	}

	public class UpdateGLViewTask extends SimpleTask {

		public UpdateGLViewTask(AbstractConnectionActivity activity) {
			super(activity);
			// setSuccess(getString(R.string.loaded_controlcenter));
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setTitle(getResources().getString(R.string.load_controlcenter));
			mProgress.setVisibility(View.VISIBLE);
		}

		@Override
		protected Exception doInBackground(String... params) {
			try {
				Set<BufferdUnit> renderUnits = mRenderer.getUnits();
				Collection<BufferdUnit> remoteUnits = mBinder.getUnits().values();
				boolean isDirty = renderUnits.size() != remoteUnits.size();
				for (BufferdUnit unit : remoteUnits)
					isDirty |= !renderUnits.contains(unit);
				if (isDirty)
					mRenderer.reloadControlCenter(mBinder);
			} catch (Exception e) {
				return e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Exception result) {
			super.onPostExecute(result);
			if (result == null && mBinder.getServer() != null)
				setTitle("Controlcenter@" + mBinder.getServer().getName());
			else
				setTitle(getResources().getString(R.string.no_conneciton));
			mRenderer.setConnections(mBinder.isConnected());
			mProgress.setVisibility(View.GONE);
		}

	}

	public class SelectControlUnit implements OnClickListener {
		private BufferdUnit mUnit;

		public void selectUnit(final BufferdUnit unit) {
			mUnit = unit;
			if (unit.mObject instanceof IMediaServer) {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						Intent intent = new Intent(ControlSceneActivity.this, MediaServerActivity.class);
						intent.putExtra(MediaServerActivity.EXTRA_MEDIA_ID, unit.mID);
						ControlSceneActivity.this.startActivity(intent);
					}
				});
			}
			if (unit.mObject instanceof ICommandAction) {
				AsyncTask<BufferdUnit, Integer, Exception> task = new AsyncTask<BufferdUnit, Integer, Exception>() {

					@Override
					protected Exception doInBackground(BufferdUnit... params) {
						try {
							ICommandAction action = (ICommandAction) unit.mObject;
							action.startAction();
						} catch (Exception e) {
							return e;
						}
						return null;
					}

					@Override
					protected void onPostExecute(Exception result) {
						if (result != null)
							new AbstractTask.ErrorDialog(ControlSceneActivity.this, result).show();
						else {
							Toast.makeText(getApplicationContext(), "Execute: " + unit.mDescription, Toast.LENGTH_SHORT)
									.show();
						}
						if (unit.mClientAction != null && unit.mClientAction.length() > 0) {
							Uri uri = Uri.parse(unit.mClientAction);
							Intent intent = new Intent(Intent.ACTION_VIEW, uri);
							startActivity(intent);

						}
					}
				};
				task.execute(unit);
			}

			if (unit.mObject instanceof IRCColor) {
				ColorPickerDialogFragment dialog = ColorPickerDialogFragment.newInstance(unit.mID.hashCode(),
						"Pick color for " + mSelecter.mUnit.mName, getString(android.R.string.ok),
						unit.mColor | 0xFF000000, false);
				dialog.show(getFragmentManager(), "colorpicker");
			}
		}

		public void selectLongClickUnit(final BufferdUnit unit) {
			mUnit = unit;
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub

					new AlertDialog.Builder(ControlSceneActivity.this)
							.setTitle(getResources().getString(R.string.command_stop))
							.setMessage(getResources().getString(R.string.command_stop_long) + " " + unit.mName + "?")
							.setPositiveButton(getResources().getString(android.R.string.yes), SelectControlUnit.this)
							.setNegativeButton(getResources().getString(android.R.string.no), null).create().show();
				}
			});
		}

		public void stopCommand(final BufferdUnit unit) {
			if (unit.mObject instanceof ICommandAction) {
				AsyncTask<BufferdUnit, Integer, Exception> task = new AsyncTask<BufferdUnit, Integer, Exception>() {

					@Override
					protected Exception doInBackground(BufferdUnit... params) {
						try {
							ICommandAction action = (ICommandAction) unit.mObject;
							action.stopAction();
						} catch (Exception e) {
							return e;
						}
						return null;
					}

					@Override
					protected void onPostExecute(Exception result) {
						if (result != null)
							new AbstractTask.ErrorDialog(ControlSceneActivity.this, result).show();
						else {
							Toast.makeText(getApplicationContext(), "Stop: " + unit.mDescription, Toast.LENGTH_SHORT)
									.show();
						}
					}
				};
				task.execute(unit);
			}

		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			stopCommand(mUnit);
		}
	}

	@Override
	public void onColorSelected(int dialogId, final int color) {
		if (mSelecter.mUnit.mObject instanceof IRCColor) {
			mSelecter.mUnit.mColor = color & 0xFFFFFF;
			new SimpleTask(this).setSuccess("Color set")
					.setDialogMessage("Set color for " + mSelecter.mUnit.mName + " to " + color)
					.setDialogtitle("Set color...").setAction(new BackgroundAction() {

						@Override
						public void run() throws Exception {
							IRCColor rccolor = (IRCColor) mSelecter.mUnit.mObject;
							rccolor.setColor(mSelecter.mUnit.mColor);
						}
					}).execute();
		}
	}

	@Override
	public void onDialogDismissed(int dialogId) {
		// TODO Auto-generated method stub

	}

}
