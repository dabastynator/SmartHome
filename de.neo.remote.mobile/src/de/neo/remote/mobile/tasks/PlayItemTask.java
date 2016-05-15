package de.neo.remote.mobile.tasks;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.session.PlaybackState;
import android.os.SystemClock;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.MediaSessionCompat.Callback;
import android.support.v4.media.session.PlaybackStateCompat;
import de.neo.remote.mobile.activities.MediaServerActivity;
import de.neo.remote.mobile.activities.MediaServerActivity.ViewerState;
import de.neo.remote.mobile.receivers.MediaButtonReceiver;
import de.neo.remote.mobile.receivers.RemoteWidgetProvider;
import de.neo.remote.mobile.services.RemoteBinder;
import de.neo.remote.mobile.services.RemoteService.StationStuff;
import de.neo.remote.mobile.services.WidgetService;

public class PlayItemTask extends AbstractTask {

	private String mItem;
	private RemoteBinder mBinder;
	private ViewerState mViewerState;
	private int mCurrentVolume;

	public PlayItemTask(MediaServerActivity activity, String item, RemoteBinder binder, ViewerState viewerState) {
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
			mediaServer.player.playPlayList(mediaServer.pls.getPlaylistFullpath(mItem));
		}
		if (mViewerState == ViewerState.PLS_ITEMS) {
			mediaServer.player.play(mItem);
		}
		if (mViewerState == ViewerState.DIRECTORIES) {
			String file = mediaServer.browser.getFullLocation() + mItem;
			mediaServer.player.play(file);
		}
		mCurrentVolume = mediaServer.player.getVolume();
	}

}