package de.remote.mobile.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import de.remote.api.PlayingBean;
import de.remote.api.PlayingBean.STATE;
import de.remote.mobile.R;
import de.remote.mobile.activities.BrowserActivity;
import de.remote.mobile.services.RemoteService.IRemoteActionListener;

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
	public void startReceive(final long size) {
		fullSize = size;
		makeDonwloadingNotification(file, 0);
		
	}

	@Override
	public void progressReceive(final long size) {
		makeDonwloadingNotification(file, ((float) size) / ((float) fullSize));

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
	public void onPlayingBeanChanged(PlayingBean playing) {
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
		if (playing.getState() == STATE.DOWN) {
			NotificationManager nm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(PLAYING_NOTIFICATION_ID);
		} else
			makePlayingNotification(title, msg);
	}

	/**
	 * create notification about playing file
	 * 
	 * @param title
	 * @param body
	 */
	protected void makePlayingNotification(String title, String body) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.browser;
		Notification notification = new Notification(icon, "Player started",
				System.currentTimeMillis());
		Intent nIntent = new Intent(context, BrowserActivity.class);
		nIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		nIntent.putExtra(BrowserActivity.EXTRA_SERVER_ID, serverID);
		PendingIntent pInent = PendingIntent
				.getActivity(context, 0, nIntent, 0);
		notification.setLatestEventInfo(context.getApplicationContext(), title,
				body, pInent);
		nm.notify(PLAYING_NOTIFICATION_ID, notification);
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
	protected void makeDonwloadingNotification(String file, float progress) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.download,
				"Download started", System.currentTimeMillis());
		notification.contentView = new RemoteViews(context.getPackageName(),
				R.layout.download_progress);
		notification.contentView.setImageViewResource(R.id.status_icon,
				R.drawable.download);
		notification.contentView.setTextViewText(R.id.status_text, "download "
				+ file);
		notification.contentView.setProgressBar(R.id.status_progress, 100,
				(int) (progress * 100), false);
		Intent nIntent = new Intent(context, BrowserActivity.class);
		nIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		nIntent.putExtra(BrowserActivity.EXTRA_SERVER_ID, serverID);
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, nIntent,
				0);
		notification.contentIntent = pIntent;
		nm.notify(DOWNLOAD_NOTIFICATION_ID, notification);
	}

}