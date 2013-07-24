package de.remote.mobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.Window;
import android.widget.Toast;
import de.newsystem.opengl.common.AbstractSceneSurfaceView;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.controlcenter.api.IControlCenter;
import de.remote.gpiopower.api.IInternetSwitch.State;
import de.remote.mobile.R;
import de.remote.mobile.util.ControlSceneRenderer;

public class OverViewActivity extends BindedActivity {

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
		renderer = new ControlSceneRenderer(getResources(), selecter);
		view = new AbstractSceneSurfaceView(this, savedInstanceState, renderer);
		setContentView(view);
		
		setTitle("connecting...");
		setProgressBarVisibility(true);
	}

	@Override
	public void onServerConnectionChanged(String serverName, int serverID) {
		setTitle("loading...");
		setProgressBarVisibility(true);
		new Thread() {
			public void run() {
				IControlCenter control = binder.getControlCenter();
				try {
					renderer.reloadControlCenter(control);
					handler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(OverViewActivity.this,
									"load center done", Toast.LENGTH_SHORT)
									.show();
							setTitle("Control Center Overview");
							setProgressBarVisibility(false);
						}
					});
				} catch (final RemoteException e) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(OverViewActivity.this,
									"error load center: " + e.getMessage(),
									Toast.LENGTH_SHORT).show();
							setTitle("Not connected");
							setProgressBarVisibility(false);
						}
					});
				}
			};
		}.start();
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
	void binderConnected() {
		// TODO Auto-generated method stub

	}

	@Override
	void startConnecting() {
		setTitle("connecting...");
		setProgressBarVisibility(true);
	}

	public class SelectMediaServer {
		public void selectMediaServer(final String name) {
			handler.post(new Runnable() {

				@Override
				public void run() {
					Intent intent = new Intent(OverViewActivity.this,
							BrowserActivity.class);
					intent.putExtra(BrowserActivity.EXTRA_MEDIA_NAME, name);
					OverViewActivity.this.startActivity(intent);
				}
			});
		}
	}

}
