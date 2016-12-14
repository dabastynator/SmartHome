package de.neo.remote.mobile.tasks;

import de.neo.remote.mobile.activities.AbstractConnectionActivity;
import de.neo.remote.mobile.persistence.MediaServerState;

public class PlayYoutubeTask extends AbstractTask {

	private String mYoutubeURL;
	private MediaServerState mMediaServer;

	public PlayYoutubeTask(AbstractConnectionActivity activity, String youtubeURL, MediaServerState mediaServer) {
		super(activity, TaskMode.DialogTask);
		mYoutubeURL = youtubeURL;
		mMediaServer = mediaServer;
	}

	@Override
	protected String getDialogMsg() {
		return mYoutubeURL;
	}

	@Override
	protected String getDialogTitle() {
		return "Start youtube stream";
	}

	@Override
	protected void onExecute() throws Exception {
		mMediaServer.playYoutube(mYoutubeURL);
	}
}
