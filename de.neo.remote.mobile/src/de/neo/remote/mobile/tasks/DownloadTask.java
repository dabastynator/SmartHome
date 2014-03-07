package de.neo.remote.mobile.tasks;

import java.io.File;

import de.neo.remote.mediaserver.api.IBrowser;
import de.neo.remote.mobile.services.PlayerBinder;
import de.neo.rmi.protokol.ServerPort;
import de.neo.rmi.transceiver.DirectoryReceiver;
import de.neo.rmi.transceiver.FileReceiver;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

public class DownloadTask extends AsyncTask<String, String, String> {

	private IBrowser browser;
	private String file;
	private String directory;
	private String serverName;
	private Exception exception;
	private PlayerBinder binder;

	public DownloadTask(IBrowser browser, String file, String directory,
			String serverName, PlayerBinder binder) {
		this.browser = browser;
		this.file = file;
		this.directory = directory;
		this.serverName = serverName;
		this.binder = binder;
	}

	@Override
	protected String doInBackground(String... params) {
		FileReceiver receiver = null;
		try {
			if (file != null) {
				ServerPort serverport = browser.publishFile(file);
				String folder = Environment.getExternalStorageDirectory()
						.toString() + File.separator + serverName.trim();
				File dir = new File(folder);
				if (!dir.exists())
					dir.mkdir();
				File newFile = new File(folder + File.separator + file.trim());
				receiver = new FileReceiver(serverport.getIp(),
						serverport.getPort(), 200000, newFile);
			}
			if (directory != null) {
				ServerPort serverport = browser.publishDirectory(directory);
				String folder = Environment.getExternalStorageDirectory()
						.toString() + File.separator + serverName.trim();
				File dir = new File(folder);
				if (!dir.exists())
					dir.mkdir();
				receiver = new DirectoryReceiver(serverport.getIp(),
						serverport.getPort(), dir);
			}
			download(receiver);
		} catch (Exception e) {
			exception = e;
		}
		return null;
	}

	/**
	 * configure receiver and start the download
	 * 
	 * @param receiver
	 */
	private void download(FileReceiver receiver) {
		binder.receiver = receiver;
		// set maximum byte size to 1MB
		receiver.setBufferSize(1000000);
		receiver.getProgressListener().add(binder.service.downloadListener);
		receiver.receiveAsync();
		onProgressUpdate(new String[] { "download started" });
	}

	@Override
	protected void onProgressUpdate(String[] values) {
		Toast.makeText(binder.service, values[0], Toast.LENGTH_SHORT).show();
	}

	public void execute() {
		execute(new String[] {});
	}

}
