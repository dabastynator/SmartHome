package de.neo.remote.mobile.fragments;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import de.neo.remote.mediaserver.api.IDVDPlayer;
import de.neo.remote.mediaserver.api.IPlayer;
import de.neo.remote.mediaserver.api.PlayingBean;
import de.neo.remote.mediaserver.api.PlayingBean.STATE;
import de.neo.remote.mobile.activities.MediaServerActivity;
import de.remote.mobile.R;

public class PlayerButtonFragment extends Fragment {

	private boolean isFullscreen;
	private Button playButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (getId() == R.id.mediaserver_fragment_button_right)
			return inflater.inflate(R.layout.mediaserver_buttons_right,
					container, false);
		return inflater.inflate(R.layout.mediaserver_buttons, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		findComponents();
	}

	private void findComponents() {
		int[] playerButtons = new int[] { R.id.button_play, R.id.button_next,
				R.id.button_pref, R.id.button_seek_bwd, R.id.button_seek_fwd,
				R.id.button_left, R.id.button_right, R.id.button_up,
				R.id.button_down, R.id.button_enter, R.id.button_vol_down,
				R.id.button_vol_up, R.id.button_full, R.id.button_quit };
		playButton = (Button) getActivity().findViewById(R.id.button_play);
		View.OnClickListener listener = new View.OnClickListener() {
			public void onClick(View v) {
				playerAction(v);
			}
		};
		for (int playerButtonId : playerButtons) {
			View playerButton = getActivity().findViewById(playerButtonId);
			if (playerButton != null) {
				playerButton.setOnClickListener(listener);
				// int btnSize = Math.min(playerButton.getLayoutParams().width,
				// playerButton.getLayoutParams().height);
				// playerButton
				// .setLayoutParams(new LinearLayout.LayoutParams(btnSize,
				// btnSize));
			}
		}
	}

	public void playerAction(final View v) {
		MediaServerActivity activity = (MediaServerActivity) getActivity();
		if (activity.binder == null
				|| activity.binder.getLatestMediaServer() == null)
			return;
		final IPlayer player = activity.binder.getLatestMediaServer().player;
		AsyncTask<String, Integer, String> task = new AsyncTask<String, Integer, String>() {

			@Override
			protected String doInBackground(String... params) {
				try {
					switch (v.getId()) {
					case R.id.button_play:
						player.playPause();
						break;
					case R.id.button_next:
						player.next();
						break;
					case R.id.button_pref:
						player.previous();
						break;
					case R.id.button_vol_up:
						player.volUp();
						break;
					case R.id.button_vol_down:
						player.volDown();
						break;
					case R.id.button_seek_bwd:
						player.seekBackwards();
						break;
					case R.id.button_seek_fwd:
						player.seekForwards();
						break;
					case R.id.button_full:
						player.fullScreen(isFullscreen = !isFullscreen);
						break;
					case R.id.button_quit:
						player.quit();
						break;
					case R.id.button_left:
						if (player instanceof IDVDPlayer)
							((IDVDPlayer) player).menuLeft();
						break;
					case R.id.button_right:
						if (player instanceof IDVDPlayer)
							((IDVDPlayer) player).menuRight();
						break;
					case R.id.button_up:
						if (player instanceof IDVDPlayer)
							((IDVDPlayer) player).menuUp();
						break;
					case R.id.button_down:
						if (player instanceof IDVDPlayer)
							((IDVDPlayer) player).menuDown();
						;
						break;
					case R.id.button_enter:
						if (player instanceof IDVDPlayer)
							((IDVDPlayer) player).menuEnter();
						break;
					}
				} catch (Exception e) {
					return e.getClass().getSimpleName() + ": " + e.getMessage();
				}
				return null;
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				if (result != null && result.length() > 0)
					Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT)
							.show();
			}
		};
		task.execute(new String[] {});
	}

	public void onPlayingBeanChanged(String mediaserver, PlayingBean bean) {
		if (playButton != null) {
			if (bean == null || bean.getState() == STATE.PLAY)
				playButton.setBackgroundResource(R.drawable.player_pause);
			else if (bean.getState() == STATE.PAUSE)
				playButton.setBackgroundResource(R.drawable.player_play);
		}
	}
}
