package de.neo.remote.mobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;
import de.neo.android.opengl.AbstractSceneSurfaceView;
import de.neo.remote.api.IInternetSwitch.State;
import de.neo.remote.api.PlayingBean;
import de.neo.remote.mobile.persistence.RemoteServer;
import de.neo.remote.mobile.services.RemoteBinder;
import de.neo.remote.mobile.tasks.AbstractTask;
import de.neo.remote.mobile.util.ControlSceneRenderer;
import de.remote.mobile.R;

public class ControlSceneActivity extends AbstractConnectionActivity {

	private AbstractSceneSurfaceView view;
	private ControlSceneRenderer renderer;
	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		super.onCreate(savedInstanceState);

		mHandler = new Handler();

		setProgressBarIndeterminateVisibility(true);
		setProgressBarVisibility(false);

		SelectMediaServer selecter = new SelectMediaServer();
		renderer = new ControlSceneRenderer(this, selecter);
		view = new AbstractSceneSurfaceView(this, savedInstanceState, renderer);
		setContentView(view);

		setTitle(getResources().getString(R.string.connecting));
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
	public void onServerConnectionChanged(RemoteServer server) {
		if (mBinder.getControlCenter() == null) {
			setTitle(getResources().getString(R.string.no_controlcenter));
			setProgressBarVisibility(false);
		} else {
			setTitle(getResources().getString(R.string.load_controlcenter));
			setProgressBarVisibility(true);
			new Thread(new UpdateGLViewRunner()).start();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.opt_control_refresh:
			setTitle(getResources().getString(R.string.str_refresh));
			setProgressBarVisibility(true);
			new Thread() {
				public void run() {
					mBinder.refreshControlCenter();
					new UpdateGLViewRunner().run();
				};
			}.start();
			break;
		}
		return super.onOptionsItemSelected(item);
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
		renderer.powerSwitchChanged(_switch, state);

	}
	
	@Override
	void onRemoteBinder(RemoteBinder mBinder) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void onStartConnecting() {
		setTitle(getResources().getString(R.string.connecting));
		setProgressBarVisibility(true);
	}

	public class UpdateGLViewRunner implements Runnable {

		@Override
		public void run() {
			try {
				renderer.reloadControlCenter(mBinder);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if (mBinder.getControlCenter() != null)
							Toast.makeText(
									ControlSceneActivity.this,
									getResources().getString(
											R.string.loaded_controlcenter),
									Toast.LENGTH_SHORT).show();
						setTitle("Controlcenter@"
								+ mBinder.getServer().getName());
						setProgressBarVisibility(false);
					}
				});
			} catch (final Exception e) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						new AbstractTask.ErrorDialog(ControlSceneActivity.this,
								e).show();
						setTitle(getResources().getString(
								R.string.no_conneciton));
						setProgressBarVisibility(false);
					}
				});
			}
		}

	}

	public class SelectMediaServer {
		public void selectMediaServer(final String id) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					Intent intent = new Intent(ControlSceneActivity.this,
							MediaServerActivity.class);
					intent.putExtra(MediaServerActivity.EXTRA_MEDIA_ID, id);
					ControlSceneActivity.this.startActivity(intent);
				}
			});
		}
	}

}
