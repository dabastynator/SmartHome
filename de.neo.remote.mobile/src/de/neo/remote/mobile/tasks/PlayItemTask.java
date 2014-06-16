package de.neo.remote.mobile.tasks;

import de.neo.remote.mobile.activities.MediaServerActivity;
import de.neo.remote.mobile.activities.MediaServerActivity.MediaState;
import de.neo.remote.mobile.activities.MediaServerActivity.ViewerState;
import de.neo.remote.mobile.fragments.BrowserFragment;
import de.neo.remote.mobile.services.PlayerBinder;
import de.neo.remote.mobile.services.RemoteService.StationStuff;
import de.remote.mobile.R;

public class PlayItemTask extends AbstractTask {

	private String item;
	private PlayerBinder binder;
	private BrowserFragment browserFragment;

	public PlayItemTask(MediaServerActivity activity, String item,
			PlayerBinder binder) {
		super(activity, TaskMode.DialogTask);
		this.item = item;
		this.binder = binder;
		this.browserFragment = (BrowserFragment) activity.getFragmentManager()
				.findFragmentById(R.id.mediaserver_fragment_browser);
	}

	@Override
	protected String getDialogTitle() {
		return "Start playing";
	}

	@Override
	protected String getDialogMsg() {
		return item;
	}

	@Override
	protected void onExecute() throws Exception {
		StationStuff mediaServer = binder.getLatestMediaServer();
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
	}
}