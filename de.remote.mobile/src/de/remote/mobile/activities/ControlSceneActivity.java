package de.remote.mobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;
import de.newsystem.opengl.common.AbstractSceneSurfaceView;
import de.remote.gpiopower.api.IInternetSwitch.State;
import de.remote.mediaserver.api.PlayingBean;
import de.remote.mobile.R;
import de.remote.mobile.util.ControlSceneRenderer;

public class ControlSceneActivity extends BindedActivity {

	private AbstractSceneSurfaceView view;
	private ControlSceneRenderer renderer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setProgressBarIndeterminateVisibility(true);
		setProgressBarVisibility(false);

		SelectMediaServer selecter = new SelectMediaServer();
		renderer = new ControlSceneRenderer(this, selecter);
		view = new AbstractSceneSurfaceView(this, savedInstanceState, renderer);
		setContentView(view);

		setTitle("connecting...");
		setProgressBarVisibility(true);
	}

	@Override
	protected void onPause() {
		super.onPause();
		view.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		view.onResume();
	}

	@Override
	public void onServerConnectionChanged(String serverName, int serverID) {
		updateGLView();
	}

	private void updateGLView() {
		if (binder.getControlCenter() == null) {
			setTitle("No control center...");
			setProgressBarVisibility(false);
		} else {
			setTitle("loading...");
			setProgressBarVisibility(true);
			new Thread(new UpdateGLViewRunner()).start();
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.opt_control_refresh:
			setTitle("refresh...");
			setProgressBarVisibility(true);
			new Thread() {
				public void run() {
					binder.refreshControlCenter();
					new UpdateGLViewRunner().run();
				};
			}.start();
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onPlayingBeanChanged(String mediaserver, PlayingBean bean) {
		renderer.setPlayingBean(mediaserver, bean);
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
		// TODO Auto-generated method stub

	}

	@Override
	void onBinderConnected() {
		// TODO Auto-generated method stub

	}

	@Override
	void onStartConnecting() {
		setTitle("connecting...");
		setProgressBarVisibility(true);
	}

	public class UpdateGLViewRunner implements Runnable {

		@Override
		public void run() {
			try {
				renderer.reloadControlCenter(binder);
				handler.post(new Runnable() {
					@Override
					public void run() {
						if (binder.getControlCenter() != null)
							Toast.makeText(ControlSceneActivity.this,
									"load center done", Toast.LENGTH_SHORT)
									.show();
						setTitle("Controlcenter@" + binder.getServerName());
						setProgressBarVisibility(false);
					}
				});
			} catch (final Exception e) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(ControlSceneActivity.this,
								"error load center: " + e.getMessage(),
								Toast.LENGTH_LONG).show();
						setTitle("Not connected");
						setProgressBarVisibility(false);
					}
				});
			}
		}

	}

	public class SelectMediaServer {
		public void selectMediaServer(final String name) {
			handler.post(new Runnable() {

				@Override
				public void run() {
					Intent intent = new Intent(ControlSceneActivity.this,
							BrowserActivity.class);
					intent.putExtra(BrowserActivity.EXTRA_MEDIA_NAME, name);
					ControlSceneActivity.this.startActivity(intent);
				}
			});
		}
	}

}
