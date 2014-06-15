package de.neo.remote.mobile.tasks;

import android.os.AsyncTask;
import android.widget.Toast;
import de.neo.remote.mobile.activities.MediaServerActivity;
import de.neo.remote.mobile.activities.MediaServerActivity.MediaState;
import de.neo.remote.mobile.activities.MediaServerActivity.ViewerState;
import de.neo.remote.mobile.fragments.BrowserFragment;
import de.neo.remote.mobile.services.PlayerBinder;
import de.neo.remote.mobile.services.RemoteService.StationStuff;
import de.remote.mobile.R;

public class PlayItemTask extends AsyncTask<String, Integer, String> {

	private MediaServerActivity browserActivity;
	private String item;
	private PlayerBinder binder;
	private BrowserFragment browserFragment;

	public PlayItemTask(MediaServerActivity browserActivity, String item,
			PlayerBinder binder) {
		this.item = item;
		this.browserActivity = browserActivity;
		this.binder = binder;
		this.browserFragment = (BrowserFragment) browserActivity.getFragmentManager()
				.findFragmentById(R.id.mediaserver_fragment_browser);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		browserActivity.startProgress("Start playing", item);
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		browserActivity.dismissProgress();
		if (result != null)
			Toast.makeText(browserActivity,
					"Error streaming youtube: " + result, Toast.LENGTH_SHORT)
					.show();
	}

	@Override
	protected String doInBackground(String... params) {
		StationStuff mediaServer = binder.getLatestMediaServer();
		try {
			if (MediaServerActivity.isImage(item)) {
				browserFragment.mediaState = MediaState.IMAGES;
				String file = mediaServer.browser.getFullLocation() + item;
				mediaServer.imageViewer.show(file);
			} else {
				if (browserFragment.viewerState == ViewerState.PLAYLISTS) {
					mediaServer.player.playPlayList(mediaServer.pls
							.getPlaylistFullpath(item));
				}
				if (browserFragment.viewerState == ViewerState.PLS_ITEMS) {
					mediaServer.player.play(item);
				}
				if (browserFragment.viewerState == ViewerState.DIRECTORIES) {
					String file = mediaServer.browser.getFullLocation() + item;
					mediaServer.player.play(file);
				}
				browserFragment.mediaState = MediaState.MUSIC_VIDEO;
			}
		} catch (Exception e) {
			return e.getClass().getSimpleName() + ": " + e.getMessage();
		}
		return null;
	}

}