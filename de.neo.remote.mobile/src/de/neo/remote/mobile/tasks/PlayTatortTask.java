package de.neo.remote.mobile.tasks;

import android.os.AsyncTask;
import android.widget.Toast;
import de.neo.remote.mediaserver.api.IPlayer;
import de.neo.remote.mobile.activities.AbstractConnectionActivity;
import de.neo.remote.mobile.services.PlayerBinder;

public class PlayTatortTask extends AsyncTask<String, Integer, String> {

	private AbstractConnectionActivity browserBase;
	private String tatortURL;
	private PlayerBinder binder;

	public PlayTatortTask(AbstractConnectionActivity browserBase, String tatortURL,
			PlayerBinder binder) {
		this.tatortURL = tatortURL;
		this.browserBase = browserBase;
		this.binder = binder;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		browserBase.startProgress("Start ARD stream", tatortURL);
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		browserBase.dismissProgress();
		if (result != null)
			Toast.makeText(browserBase, "Error streaming ARD: " + result,
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
				player.playFromArdMediathek(tatortURL);
			} else
				throw new Exception("no player selected");
		} catch (Exception e) {
			return e.getClass().getSimpleName() + ": " + e.getMessage();
		}
		return null;
	}

}
