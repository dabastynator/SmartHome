package de.neo.smarthome.mobile.tasks;

import de.neo.smarthome.mobile.api.IWebMediaServer;
import de.neo.smarthome.mobile.api.IWebMediaServer.BeanFileSystem;
import de.neo.smarthome.mobile.api.IWebMediaServer.BeanPlaylist;
import de.neo.smarthome.mobile.api.IWebMediaServer.BeanPlaylistItem;
import de.neo.smarthome.mobile.activities.WebAPIActivity;
import de.neo.smarthome.mobile.persistence.MediaServerState;

public class PlayItemTask extends AbstractTask {

	private MediaServerState mServer;
	private BeanFileSystem mFile;
	private BeanPlaylist mPlaylist;
	private BeanPlaylistItem mItem;

	public PlayItemTask(WebAPIActivity activity, MediaServerState server, BeanFileSystem file) {
		super(activity, TaskMode.DialogTask);
		mFile = file;
		mServer = server;
	}

	public PlayItemTask(WebAPIActivity activity, MediaServerState server, BeanPlaylist playlist) {
		super(activity, TaskMode.DialogTask);
		mPlaylist = playlist;
		mServer = server;
	}

	public PlayItemTask(WebAPIActivity activity, MediaServerState server, BeanPlaylistItem item) {
		super(activity, TaskMode.DialogTask);
		mItem = item;
		mServer = server;
	}

	@Override
	protected String getDialogTitle() {
		return "Start playing";
	}

	@Override
	protected String getDialogMsg() {
		if (mItem != null)
			return mItem.getName();
		else if (mPlaylist != null)
			return mPlaylist.getName();
		else if (mFile != null)
			return mFile.getName();
		else
			return "Unknown";
	}

	@Override
	protected void onExecute() throws Exception {
		if (mItem != null)
			mServer.playFile(mItem.getPath());
		else if (mPlaylist != null)
			mServer.playPlaylist(mPlaylist.getName());
		else if (mFile != null)
			mServer.playFile(mServer.getBrowserLocation() + IWebMediaServer.FileSeparator + mFile.getName());
		else
			throw new IllegalStateException("No item to play specified");
	}

}