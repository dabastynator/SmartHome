package de.neo.remote.mobile.tasks;

import de.neo.remote.api.IPlayer;
import de.neo.remote.mobile.activities.AbstractConnectionActivity;
import de.neo.remote.mobile.services.RemoteBinder;

public class PlayYoutubeTask extends AbstractTask {

	private String youtubeURL;
	private RemoteBinder binder;

	public PlayYoutubeTask(AbstractConnectionActivity activity,
			String youtubeURL, RemoteBinder binder) {
		super(activity, TaskMode.DialogTask);
		this.youtubeURL = youtubeURL;
		this.binder = binder;
	}

	@Override
	protected String getDialogMsg() {
		return youtubeURL;
	}

	@Override
	protected String getDialogTitle() {
		return "Start youtube stream";
	}

	@Override
	protected void onExecute() throws Exception {
		if (binder == null)
			throw new Exception("not bindet");
		if (binder.getLatestMediaServer() == null)
			throw new Exception("no mediaserver selected");
		IPlayer player = binder.getLatestMediaServer().player;
		if (player != null) {
			player.playFromYoutube(youtubeURL);
		} else
			throw new Exception("no player selected");
	}
}
