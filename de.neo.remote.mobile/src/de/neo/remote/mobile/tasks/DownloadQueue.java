package de.neo.remote.mobile.tasks;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.widget.Toast;
import de.neo.remote.api.IWebMediaServer;
import de.neo.remote.api.IWebMediaServer.BeanDownload;
import de.neo.remote.api.IWebMediaServer.BeanDownload.DownloadType;
import de.neo.remote.mobile.activities.MediaServerActivity;
import de.neo.rmi.transceiver.DirectoryReceiver;
import de.neo.rmi.transceiver.FileReceiver;
import de.neo.rmi.transceiver.ReceiverProgress;
import de.remote.mobile.R;

public class DownloadQueue extends Thread implements ReceiverProgress {

	public static final int BUFFER_SIZE = 1024 * 512;
	public static final int PROGRESS_STEP = 1024 * 1024;
	public static final int DOWNLOAD_NOTIFICATION_ID = 2;

	private BlockingQueue<Runnable> mJobs;
	private boolean mRunning;
	private Context mContext;
	private long mFullDownloadSize;
	private String mID;
	private Handler mHandler;

	public DownloadQueue(Context context, Handler handler) {
		mRunning = true;
		mJobs = new LinkedBlockingQueue<>();
		mContext = context;
		mHandler = handler;
	}

	public void setRunning(boolean running) {
		mRunning = running;
		mJobs.add(new EmptyJob());
	}

	public void download(IWebMediaServer webMediaServer, String id, String destiny, Object download) {
		if (download instanceof String) {
			DownloadJob job = new DownloadJob(webMediaServer, id, destiny, (String) download);
			mJobs.add(job);
		} else if (download instanceof String[]) {
			String[] files = (String[]) download;
			for (String file : files) {
				DownloadJob job = new DownloadJob(webMediaServer, id, destiny, file);
				mJobs.add(job);
			}
		} else
			throw new IllegalArgumentException("Expect String or String[] to download. Can't donwload: " + download);
	}

	@Override
	public void run() {
		while (mRunning) {
			try {
				Runnable job = mJobs.take();
				job.run();
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}

	public static class EmptyJob implements Runnable {
		@Override
		public void run() {
		}
	}

	class DownloadJob implements Runnable {

		private String mFile;
		private IWebMediaServer mWebServer;
		private String mID;
		private String mDestiny;

		public DownloadJob(IWebMediaServer webMediaServer, String id, String destiny, String file) {
			mWebServer = webMediaServer;
			mID = id;
			mFile = file;
			mDestiny = destiny;
		}

		@Override
		public void run() {
			try {
				doDownload();
			} catch (final Exception e) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(mContext, "Error occurred while downloading: " + e.getMessage(),
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		}

		protected void doDownload() throws Exception {
			DownloadQueue.this.mID = mID;
			BeanDownload download = mWebServer.publishForDownload(mID, mFile);
			FileReceiver receiver = null;
			String folder = Environment.getExternalStorageDirectory().toString() + File.separator + mDestiny;
			File dir = new File(folder);
			if (!dir.exists())
				dir.mkdir();
			String localFile = mFile.trim();
			if (localFile.contains(IWebMediaServer.FileSeparator))
				localFile = localFile.substring(
						localFile.lastIndexOf(IWebMediaServer.FileSeparator) + IWebMediaServer.FileSeparator.length());
			if (download.getType() == DownloadType.File) {
				File newFile = new File(folder + File.separator + localFile);
				receiver = new FileReceiver(download.getIP(), download.getPort(), PROGRESS_STEP, newFile);
			}
			if (download.getType() == DownloadType.Directory) {
				receiver = new DirectoryReceiver(download.getIP(), download.getPort(), dir);
			}
			receiver.setBufferSize(BUFFER_SIZE);
			receiver.getProgressListener().add(DownloadQueue.this);
			receiver.receiveSync();
		}

	}

	@Override
	public void startReceive(long size, String file) {
		mFullDownloadSize = size;
		makeLoadNotification(mContext.getResources().getString(R.string.str_download), file, 0, R.drawable.download,
				mID);
	}

	@Override
	public void progressReceive(long size, String file) {
		makeLoadNotification(mContext.getResources().getString(R.string.str_download), file,
				((float) size) / ((float) mFullDownloadSize), R.drawable.download, mID);
	}

	@Override
	public void endReceive(long size) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(mContext, "Download finished", Toast.LENGTH_SHORT).show();
			}
		});
		NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(DOWNLOAD_NOTIFICATION_ID);
	}

	@Override
	public void exceptionOccurred(final Exception e) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(mContext, "Error occurred while downloading: " + e.getMessage(), Toast.LENGTH_SHORT)
						.show();
			}
		});
		NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(DOWNLOAD_NOTIFICATION_ID);
	}

	@Override
	public void downloadCanceled() {
		NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(DOWNLOAD_NOTIFICATION_ID);
	}

	protected void makeLoadNotification(String title, String text, float progress, int imgResource, String id) {
		NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent nIntent = new Intent(mContext, MediaServerActivity.class);
		nIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		nIntent.putExtra(MediaServerActivity.EXTRA_SERVER_ID, id);
		PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, nIntent, 0);

		Builder builder = new NotificationCompat.Builder(mContext);
		builder.setContentTitle(title);
		builder.setContentText(text);
		builder.setProgress(100, (int) (progress * 100), false);
		builder.setSmallIcon(imgResource);
		builder.setContentIntent(pIntent);
		nm.notify(DOWNLOAD_NOTIFICATION_ID, builder.build());
	}

}
