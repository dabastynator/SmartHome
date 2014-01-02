package de.neo.remote.mobile.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import de.neo.remote.gpiopower.api.IInternetSwitch.State;
import de.neo.remote.mediaserver.api.PlayingBean;
import de.neo.remote.mediaserver.api.PlayingBean.STATE;
import de.neo.remote.mobile.activities.BrowserActivity;
import de.neo.remote.mobile.services.RemoteService.IRemoteActionListener;
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
		makeLoadNotification("download " + file, 0, R.drawable.download);

	}

	@Override
	public void progressReceive(final long size, String file) {
		makeLoadNotification("download " + file, ((float) size)
				/ ((float) fullSize), R.drawable.download);

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
	protected void makeLoadNotification(String text, float progress,
			int imgResource) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(imgResource,
				"Download started", System.currentTimeMillis());
		notification.contentView = new RemoteViews(context.getPackageName(),
				R.layout.download_progress);
		notification.contentView.setImageViewResource(R.id.status_icon,
				imgResource);
		notification.contentView.setTextViewText(R.id.status_text, text);
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

	@Override
	public void onPowerSwitchChange(String switchName, State state) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startSending(long size) {
		fullSize = size;
		makeLoadNotification("upload " + file, 0, R.drawable.upload);
	}

	@Override
	public void progressSending(long size) {
		makeLoadNotification("upload " + file, ((float) size)
				/ ((float) fullSize), R.drawable.upload);
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