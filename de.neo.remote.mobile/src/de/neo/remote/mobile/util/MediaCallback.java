package de.neo.remote.mobile.util;

import android.content.Context;
import android.support.v4.media.session.MediaSessionCompat.Callback;

class MediaCallback extends Callback {

	private Context mContext;

	public MediaCallback(Context context) {
		mContext = context;
	}

	@Override
	public void onPlay() {
		// Handled by broadcast-receiver
		/*
		 * Intent playIntent = new Intent(mContext, WidgetService.class);
		 * playIntent.setAction(RemoteWidgetProvider.ACTION_PLAY);
		 * mContext.startService(playIntent);
		 */
	}

	@Override
	public void onPause() {
		// Handled by broadcast-receiver
		/*
		 * Intent playIntent = new Intent(mContext, WidgetService.class);
		 * playIntent.setAction(RemoteWidgetProvider.ACTION_PLAY);
		 * mContext.startService(playIntent);
		 */
	}

	@Override
	public void onSkipToNext() {
		// Handled by broadcast-receiver
		/*
		 * Intent playIntent = new Intent(mContext, WidgetService.class);
		 * playIntent.setAction(RemoteWidgetProvider.ACTION_NEXT);
		 * mContext.startService(playIntent);
		 */
	}

	@Override
	public void onSkipToPrevious() {
		// Handled by broadcast-receiver
		/*
		 * Intent playIntent = new Intent(mContext, WidgetService.class);
		 * playIntent.setAction(RemoteWidgetProvider.ACTION_PREV);
		 * mContext.startService(playIntent);
		 */
	}

	@Override
	public void onStop() {
		// Handled by broadcast-receiver
		/*
		 * Intent playIntent = new Intent(mContext, WidgetService.class);
		 * playIntent.setAction(RemoteWidgetProvider.ACTION_STOP);
		 * mContext.startService(playIntent);
		 */
	}
}