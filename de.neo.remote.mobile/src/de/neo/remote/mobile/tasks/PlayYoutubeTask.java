package de.neo.remote.mobile.tasks;

import android.os.AsyncTask;
import android.widget.Toast;
import de.neo.remote.mediaserver.api.IPlayer;
import de.neo.remote.mobile.activities.BrowserBase;
import de.neo.remote.mobile.services.PlayerBinder;

public class PlayYoutubeTask extends AsyncTask<String, Integer, String> {

	private BrowserBase browserBase;
	private String youtubeURL;
	private PlayerBinder binder;

	public PlayYoutubeTask(BrowserBase browserBase, String youtubeURL,
			PlayerBinder binder) {
		this.youtubeURL = youtubeURL;
		this.browserBase = browserBase;
		this.binder = binder;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		browserBase.startProgress("Start youtube stream", youtubeURL);
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		browserBase.dismissProgress();
		if (result != null)
			Toast.makeText(browserBase, "Error streaming youtube: " + result,
					Toast.LENGTH_SHORT).show();
	}

	@Override
	protected String doInBackground(String... params) {
		try {
			if (binder == null)
				throw new Exception("not bindet");
			if (binder.getLatestMediaServer() == null)
				throw new Exception("no mediaserver selected");
			IPlayer player = binder.getLatestMediaServer().player;
			if (player != null) {
				player.playFromYoutube(youtubeURL);
			} else
				throw new Exception("no player selected");
		} catch (Exception e) {
			return e.getClass().getSimpleName() + ": " + e.getMessage();
		}
		return null;
	}

}
