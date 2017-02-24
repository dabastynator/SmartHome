package de.neo.smarthome.mobile.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import de.neo.smarthome.api.PlayingBean;
import de.neo.smarthome.api.PlayingBean.STATE;
import de.neo.smarthome.mobile.activities.MediaServerActivity;
import de.neo.smarthome.mobile.persistence.MediaServerState;
import de.neo.smarthome.mobile.tasks.AbstractTask;
import de.remote.mobile.R;

public class PlayerButtonFragment extends Fragment {

	private boolean isFullscreen;
	private Button playButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (getId() == R.id.mediaserver_fragment_button_right)
			return inflater.inflate(R.layout.mediaserver_buttons_right, container, false);
		return inflater.inflate(R.layout.mediaserver_buttons, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		findComponents();
	}

	private void findComponents() {
		int[] playerButtons = new int[] { R.id.button_play, R.id.button_next, R.id.button_pref, R.id.button_seek_bwd,
				R.id.button_seek_fwd, R.id.button_vol_down, R.id.button_vol_up, R.id.button_full, R.id.button_quit };
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
			}
		}
	}

	public void playerAction(final View v) {
		MediaServerActivity activity = (MediaServerActivity) getActivity();
		final MediaServerState state = activity.getMediaServerState();
		if (state == null)
			return;
		AsyncTask<String, Integer, Exception> task = new AsyncTask<String, Integer, Exception>() {

			PlayingBean mPlaying = null;

			@Override
			protected Exception doInBackground(String... params) {
				try {
					switch (v.getId()) {
					case R.id.button_play:
						mPlaying = state.playPause();
						break;
					case R.id.button_next:
						mPlaying = state.next();
						break;
					case R.id.button_pref:
						mPlaying = state.previous();
						break;
					case R.id.button_vol_up:
						mPlaying = state.volUp();
						break;
					case R.id.button_vol_down:
						mPlaying = state.volDown();
						break;
					case R.id.button_seek_bwd:
						mPlaying = state.seekBackwards();
						break;
					case R.id.button_seek_fwd:
						mPlaying = state.seekForwards();
						break;
					case R.id.button_full:
						mPlaying = state.fullScreen(isFullscreen = !isFullscreen);
						break;
					case R.id.button_quit:
						mPlaying = state.stop();
						break;
					}
				} catch (Exception e) {
					return e;
				}
				return null;
			}

			@Override
			protected void onPostExecute(Exception exception) {
				super.onPostExecute(exception);
				if (exception != null)
					new AbstractTask.ErrorDialog(getContext(), exception).show();
				if (mPlaying != null)
					onPlayingBeanChanged(state.getMediaServerID(), mPlaying);
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
