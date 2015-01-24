package de.neo.remote.mobile.tasks;

import de.neo.remote.mobile.activities.AbstractConnectionActivity;
import de.neo.remote.mobile.services.PlayerBinder;
import de.neo.remote.mobile.util.BufferBrowser;

public class PlayTatortTask extends AbstractTask {

	private PlayerBinder binder;
	private int mDocumentId;

	public PlayTatortTask(AbstractConnectionActivity activity,
			int documentID, PlayerBinder binder) {
		super(activity, TaskMode.DialogTask);
		this.mDocumentId = documentID;
		this.binder = binder;
	}

	@Override
	protected String getDialogMsg() {
		return "ID = " + mDocumentId;
	}

	@Override
	protected String getDialogTitle() {
		return "Download ARD stream";
	}

	@Override
	protected void onExecute() throws Exception {
		if (binder == null)
			throw new Exception("not bindet");
		if (binder.getLatestMediaServer() == null)
			throw new Exception("no mediaserver selected");
		BufferBrowser browser = binder.getLatestMediaServer().browser;
		if (browser != null) {
			browser.downloadFromARDMediathek(mDocumentId);
		} else
			throw new Exception("no player selected");
	}
}
