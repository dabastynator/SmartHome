package de.neo.remote.mobile.tasks;

import java.io.File;

import android.os.Environment;
import de.neo.remote.api.IBrowser;
import de.neo.remote.mobile.activities.WebAPIActivity;
import de.neo.rmi.protokol.ServerPort;
import de.neo.rmi.transceiver.DirectoryReceiver;
import de.neo.rmi.transceiver.FileReceiver;

public class DownloadTask extends AbstractTask {

	public DownloadTask(WebAPIActivity activity, IBrowser browser, String file, String directory, String serverName) {
		super(activity, TaskMode.ToastTask);
		this.browser = browser;
		this.file = file;
		this.directory = directory;
		this.serverName = serverName;
	}

	public static final int BUFFER_SIZE = 1024 * 512;
	public static final int PROGRESS_STEP = 1024 * 1024;

	private IBrowser browser;
	private String file;
	private String directory;
	private String serverName;

	@Override
	protected void onExecute() throws Exception {
		FileReceiver receiver = null;
		if (file != null) {
			ServerPort serverport = browser.publishFile(file);
			String folder = Environment.getExternalStorageDirectory().toString() + File.separator + serverName.trim();
			File dir = new File(folder);
			if (!dir.exists())
				dir.mkdir();
			File newFile = new File(folder + File.separator + file.trim());
			receiver = new FileReceiver(serverport.getIp(), serverport.getPort(), PROGRESS_STEP, newFile);
		}
		if (directory != null) {
			ServerPort serverport = browser.publishDirectory(directory);
			String folder = Environment.getExternalStorageDirectory().toString() + File.separator + serverName.trim();
			File dir = new File(folder);
			if (!dir.exists())
				dir.mkdir();
			receiver = new DirectoryReceiver(serverport.getIp(), serverport.getPort(), dir);
		}
		receiver.setBufferSize(BUFFER_SIZE);
		// receiver.getProgressListener().add(binder.mService.mDownloadListener);
		receiver.receiveSync();
	}

	@Override
	protected String getDialogTitle() {
		return "download started";
	}

}
