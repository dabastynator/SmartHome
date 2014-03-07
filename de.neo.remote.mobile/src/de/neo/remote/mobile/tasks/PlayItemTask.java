package de.neo.remote.mobile.tasks;

import android.os.AsyncTask;
import android.widget.Toast;
import de.neo.remote.mobile.activities.BrowserActivity;
import de.neo.remote.mobile.activities.BrowserBase.MediaState;
import de.neo.remote.mobile.activities.BrowserBase.ViewerState;
import de.neo.remote.mobile.services.PlayerBinder;
import de.neo.remote.mobile.services.RemoteService.StationStuff;

public class PlayItemTask extends AsyncTask<String, Integer, String> {

	private BrowserActivity browserActivity;
	private String item;
	private PlayerBinder binder;

	public PlayItemTask(BrowserActivity browserActivity, String item,
			PlayerBinder binder) {
		this.item = item;
		this.browserActivity = browserActivity;
		this.binder = binder;
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
			if (BrowserActivity.isImage(item)) {
				browserActivity.mediaState = MediaState.IMAGES;
				String file = mediaServer.browser.getFullLocation() + item;
				mediaServer.imageViewer.show(file);
			} else {
				if (browserActivity.viewerState == ViewerState.PLAYLISTS) {
					mediaServer.player.playPlayList(mediaServer.pls
							.getPlaylistFullpath(item));
				}
				if (browserActivity.viewerState == ViewerState.PLS_ITEMS) {
					mediaServer.player.play(item);
				}
				if (browserActivity.viewerState == ViewerState.DIRECTORIES) {
					String file = mediaServer.browser.getFullLocation() + item;
					mediaServer.player.play(file);
				}
				browserActivity.mediaState = MediaState.MUSIC_VIDEO;
			}
		} catch (Exception e) {
			return e.getClass().getSimpleName() + ": " + e.getMessage();
		}
		return null;
	}

}