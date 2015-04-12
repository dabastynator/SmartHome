package de.neo.remote.mobile.util;

import java.nio.IntBuffer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import de.neo.remote.api.IInternetSwitch.State;
import de.neo.remote.api.PlayingBean;
import de.neo.remote.api.PlayingBean.STATE;
import de.neo.remote.mobile.activities.MediaServerActivity;
import de.neo.remote.mobile.receivers.RemoteWidgetProvider;
import de.neo.remote.mobile.services.RemoteService.IRemoteActionListener;
import de.neo.remote.mobile.services.WidgetService;
import de.remote.mobile.R;

/**
 * The notification handler handles the notifications for actual playing file
 * and download progress.
 * 
 * @author sebastian
 */
public class NotificationHandler implements IRemoteActionListener {

	/**
	 * id of the playing notification
	 */
	protected static final int PLAYING_NOTIFICATION_ID = 1;

	/**
	 * id of the download notification
	 */
	public static final int DOWNLOAD_NOTIFICATION_ID = 2;

	/**
	 * current downloading file
	 */
	private String file;

	/**
	 * size of the whole file
	 */
	private long fullSize;

	/**
	 * the context to make notifications
	 */
	private Context context;

	private int serverID;

	public NotificationHandler(Context context) {
		this.context = context;
	}

	/**
	 * set new donwloading file
	 * 
	 * @param file
	 */
	public void setFile(String file) {
		this.file = file;
	}

	@Override
	public void startReceive(final long size, String file) {
		fullSize = size;
		makeLoadNotification(
				context.getResources().getString(R.string.str_download), file,
				0, R.drawable.download);

	}

	@Override
	public void progressReceive(final long size, String file) {
		makeLoadNotification(
				context.getResources().getString(R.string.str_download), file,
				((float) size) / ((float) fullSize), R.drawable.download);

	}

	@Override
	public void endReceive(final long size) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(DOWNLOAD_NOTIFICATION_ID);

	}

	@Override
	public void exceptionOccurred(final Exception e) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(DOWNLOAD_NOTIFICATION_ID);

	}

	@Override
	public void downloadCanceled() {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(DOWNLOAD_NOTIFICATION_ID);

	}

	@Override
	public void onPlayingBeanChanged(String mediaserver, PlayingBean playing) {
		if (playing == null || playing.getState() == STATE.DOWN) {
			NotificationManager nm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(PLAYING_NOTIFICATION_ID);
			return;
		}
		StringBuilder sb = new StringBuilder();
		String t = "Playing";
		if (playing.getTitle() != null && playing.getTitle().length() > 0)
			t = playing.getTitle();
		else if (playing.getFile() != null)
			t = playing.getFile();
		if (playing.getArtist() != null && playing.getArtist().length() > 0)
			sb.append(playing.getArtist());
		if (playing.getAlbum() != null && playing.getAlbum().length() > 0)
			sb.append(" <" + playing.getAlbum() + ">");
		final String msg = sb.toString();
		final String title = t;
		Bitmap thumbnail = null;
		if (playing.getThumbnailWidth() * playing.getThumbnailHeight() > 0
				&& playing.getThumbnailRGB() != null) {
			thumbnail = Bitmap.createBitmap(playing.getThumbnailWidth(),
					playing.getThumbnailHeight(), Bitmap.Config.RGB_565);
			IntBuffer buf = IntBuffer.wrap(playing.getThumbnailRGB()); // data
																		// is my
																		// array
			thumbnail.copyPixelsFromBuffer(buf);
		}
		if (playing.getState() == STATE.DOWN) {
			NotificationManager nm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(PLAYING_NOTIFICATION_ID);
		} else
			makePlayingNotification(title, msg, thumbnail,
					playing.getStartTime(), playing.getState() == STATE.PLAY);
	}

	/**
	 * create notification about playing file
	 * 
	 * @param title
	 * @param body
	 * @param thumbnail
	 */
	protected void makePlayingNotification(String title, String body,
			Bitmap thumbnail, long when, boolean playing) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent nIntent = new Intent(context, MediaServerActivity.class);
		nIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		nIntent.putExtra(MediaServerActivity.EXTRA_SERVER_ID, serverID);
		PendingIntent pInent = PendingIntent
				.getActivity(context, 0, nIntent, 0);
		Intent playIntent = new Intent(context, WidgetService.class);
		playIntent.setAction(RemoteWidgetProvider.ACTION_PLAY);
		PendingIntent playPending = PendingIntent.getService(context, 0,
				playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		Intent nextIntent = new Intent(context, WidgetService.class);
		nextIntent.setAction(RemoteWidgetProvider.ACTION_VOLUP);
		PendingIntent nextPending = PendingIntent.getService(context, 0,
				nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		Intent prevIntent = new Intent(context, WidgetService.class);
		prevIntent.setAction(RemoteWidgetProvider.ACTION_VOLDOWN);
		PendingIntent prevPending = PendingIntent.getService(context, 0,
				prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Builder builder = new NotificationCompat.Builder(context);
		builder.setSmallIcon(R.drawable.remote_icon);
		builder.setContentTitle(title);
		builder.setContentText(body);
		builder.setContentIntent(pInent);
		builder.setWhen(when);
		if (playing)
			builder.addAction(R.drawable.player_small_pause,
					context.getString(R.string.player_pause), playPending);
		else
			builder.addAction(R.drawable.player_small_play,
					context.getString(R.string.str_play), playPending);
		builder.addAction(R.drawable.player_small_vol_down,
				context.getString(R.string.player_vol_down), prevPending);
		builder.addAction(R.drawable.player_small_vol_up,
				context.getString(R.string.player_vol_up), nextPending);
		if (thumbnail != null)
			builder.setLargeIcon(thumbnail);
		nm.notify(PLAYING_NOTIFICATION_ID, builder.build());
	}

	public void removeNotification() {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(PLAYING_NOTIFICATION_ID);
		nm.cancel(DOWNLOAD_NOTIFICATION_ID);
	}

	@Override
	public void onServerConnectionChanged(String serverName, int serverID) {
		this.serverID = serverID;
	}

	@Override
	public void onStopService() {
		removeNotification();
	}

	/**
	 * create notification about downloading file
	 * 
	 * @param title
	 * @param body
	 */
	protected void makeLoadNotification(String title, String text,
			float progress, int imgResource) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent nIntent = new Intent(context, MediaServerActivity.class);
		nIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		nIntent.putExtra(MediaServerActivity.EXTRA_SERVER_ID, serverID);
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, nIntent,
				0);

		Builder builder = new NotificationCompat.Builder(context);
		builder.setContentTitle(title);
		builder.setContentText(text);
		builder.setProgress(100, (int) (progress * 100), false);
		builder.setSmallIcon(imgResource);
		builder.setContentIntent(pIntent);
		nm.notify(DOWNLOAD_NOTIFICATION_ID, builder.build());
	}

	@Override
	public void onPowerSwitchChange(String switchName, State state) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startSending(long size) {
		fullSize = size;
		makeLoadNotification(
				context.getResources().getString(R.string.str_upload), file, 0,
				R.drawable.upload);
	}

	@Override
	public void progressSending(long size) {
		makeLoadNotification(
				context.getResources().getString(R.string.str_upload), file,
				((float) size) / ((float) fullSize), R.drawable.upload);
	}

	@Override
	public void endSending(long size) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(DOWNLOAD_NOTIFICATION_ID);
	}

	@Override
	public void sendingCanceled() {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(DOWNLOAD_NOTIFICATION_ID);
	}

}