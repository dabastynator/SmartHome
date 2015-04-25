package de.neo.remote.mobile.tasks;

import de.neo.remote.mobile.activities.MediaServerActivity;
import de.neo.remote.mobile.activities.MediaServerActivity.ViewerState;
import de.neo.remote.mobile.services.RemoteBinder;
import de.neo.remote.mobile.services.RemoteService.StationStuff;

public class PlayItemTask extends AbstractTask {

	private String mItem;
	private RemoteBinder mBinder;
	private ViewerState mViewerState;

	public PlayItemTask(MediaServerActivity activity, String item,
			RemoteBinder binder, ViewerState viewerState) {
		super(activity, TaskMode.DialogTask);
		this.mItem = item;
		this.mBinder = binder;
		mViewerState = viewerState;
	}

	@Override
	protected String getDialogTitle() {
		return "Start playing";
	}

	@Override
	protected String getDialogMsg() {
		return mItem;
	}

	@Override
	protected void onExecute() throws Exception {
		StationStuff mediaServer = mBinder.getLatestMediaServer();
		if (mViewerState == ViewerState.PLAYLISTS) {
			mediaServer.player.playPlayList(mediaServer.pls
					.getPlaylistFullpath(mItem));
		}
		if (mViewerState == ViewerState.PLS_ITEMS) {
			mediaServer.player.play(mItem);
		}
		if (mViewerState == ViewerState.DIRECTORIES) {
			String file = mediaServer.browser.getFullLocation() + mItem;
			mediaServer.player.play(file);
		}
	}
}