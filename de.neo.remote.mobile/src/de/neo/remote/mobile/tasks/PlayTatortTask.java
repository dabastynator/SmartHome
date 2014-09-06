package de.neo.remote.mobile.tasks;

import de.neo.remote.api.IPlayer;
import de.neo.remote.mobile.activities.AbstractConnectionActivity;
import de.neo.remote.mobile.services.PlayerBinder;

public class PlayTatortTask extends AbstractTask {

	private String tatortURL;
	private PlayerBinder binder;

	public PlayTatortTask(AbstractConnectionActivity activity,
			String tatortURL, PlayerBinder binder) {
		super(activity, TaskMode.DialogTask);
		this.tatortURL = tatortURL;
		this.binder = binder;
	}

	@Override
	protected String getDialogMsg() {
		return tatortURL;
	}

	@Override
	protected String getDialogTitle() {
		return "Start ARD stream";
	}

	@Override
	protected void onExecute() throws Exception {
		if (binder == null)
			throw new Exception("not bindet");
		if (binder.getLatestMediaServer() == null)
			throw new Exception("no mediaserver selected");
		IPlayer player = binder.getLatestMediaServer().player;
		if (player != null) {
			player.playFromArdMediathek(tatortURL);
		} else
			throw new Exception("no player selected");
	}
}
