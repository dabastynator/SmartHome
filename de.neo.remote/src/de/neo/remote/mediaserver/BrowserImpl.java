package de.neo.remote.mediaserver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.neo.remote.api.IBrowser;
import de.neo.remote.api.IThumbnailListener;
import de.neo.remote.mediaserver.ThumbnailHandler.ImageThumbnailJob;
import de.neo.remote.mediaserver.ThumbnailHandler.ThumbnailJob;
import de.neo.remote.mediaserver.ThumbnailHandler.ThumbnailListener;
import de.neo.rmi.api.Oneway;
import de.neo.rmi.api.Server;
import de.neo.rmi.protokol.RemoteException;
import de.neo.rmi.protokol.ServerPort;
import de.neo.rmi.transceiver.DirectorySender;
import de.neo.rmi.transceiver.FileReceiver;
import de.neo.rmi.transceiver.FileSender;
import de.neo.rmi.transceiver.SenderProgress;

public class BrowserImpl implements IBrowser, ThumbnailListener {

	public static final int DOWNLOAD_PORT = 5033;

	private String location;
	private String root;
	private List<String> currentFiles;

	/**
	 * Create new browser
	 * 
	 * @param path
	 *            to root directory for the browser
	 */
	public BrowserImpl(String directory) {
		if (!directory.endsWith(File.separator))
			directory += File.separator;
		root = location = directory;
		currentFiles = Collections.synchronizedList(new ArrayList<String>());
		ThumbnailHandler.instance().calculationListener().add(this);
	}

	@Override
	public boolean goBack() {
		if (root.equals(location))
			return false;
		location = location.substring(0, location.lastIndexOf(File.separator));
		location = location.substring(0,
				location.lastIndexOf(File.separator) + 1);
		return true;
	}

	@Override
	public void goTo(String directory) {
		if (!location.endsWith(File.separator))
			location += File.separator;
		location += directory + File.separator;
	}

	@Override
	public String[] getDirectories() {
		List<String> list = new ArrayList<String>();
		for (String str : new File(location).list())
			if (new File(location + str).isDirectory())
				if (str.length() > 0 && str.charAt(0) != '.')
					list.add(str);
		return list.toArray(new String[] {});
	}

	@Override
	public String[] getFiles() {
		List<String> list = new ArrayList<String>();
		for (String str : new File(location).list())
			if (new File(location + str).isFile())
				if (str.length() > 0 && str.charAt(0) != '.')
					list.add(str);
		return list.toArray(new String[] {});
	}

	@Override
	public String getLocation() {
		if (location.lastIndexOf(File.separator) >= 0) {
			String str = location.substring(0,
					location.lastIndexOf(File.separator));
			return str.substring(str.lastIndexOf(File.separator) + 1);
		}
		return location;
	}

	@Override
	public String getFullLocation() {
		return location;
	}

	@Override
	public boolean delete(String file) throws RemoteException {
		throw new RemoteException("delete", "not supported");
	}

	@Override
	public ServerPort publishFile(String file) throws RemoteException,
			IOException {
		FileSender sender = new FileSender(new File(location + file),
				DOWNLOAD_PORT, 1);
		sender.getProgressListener().add(new BrowserSendListener());
		sender.sendAsync();
		ServerPort serverport = new ServerPort(Server.getServer()
				.getServerPort().getIp(), DOWNLOAD_PORT);
		return serverport;
	}

	@Override
	public ServerPort publishDirectory(String directory)
			throws RemoteException, IOException {
		DirectorySender sender = new DirectorySender(new File(location
				+ directory), DOWNLOAD_PORT, 1);
		sender.getProgressListener().add(new BrowserSendListener());
		sender.sendAsync();
		ServerPort serverport = new ServerPort(Server.getServer()
				.getServerPort().getIp(), DOWNLOAD_PORT);
		return serverport;
	}

	@Override
	public void updloadFile(String file, String serverIp, int port)
			throws RemoteException {
		FileReceiver receiver = new FileReceiver(serverIp, port, new File(
				location + file));
		receiver.receiveAsync();
	}

	@Override
	public void fireThumbnails(IThumbnailListener listener, int width,
			int height) throws RemoteException {
		currentFiles.clear();
		for (String fileName : new File(location).list()) {
			String absoluteFileName = location + fileName;
			currentFiles.add(absoluteFileName);
			File file = new File(absoluteFileName);
			if (file.isFile() && fileName.length() > 3) {
				String extension = fileName.toUpperCase().substring(
						fileName.length() - 3);
				if (extension.equals("JPG") || extension.equals("PNG")
						|| extension.equals("GIF")) {
					BrowserThumbnailJob job = new BrowserThumbnailJob(file,
							width, height, listener, fileName);
					ThumbnailHandler.instance().queueThumbnailJob(job);
				}
			}
		}
	}

	public class BrowserSendListener implements SenderProgress {

		@Override
		public void startSending(long size) {
		}

		@Override
		public void progressSending(long size) {
		}

		@Override
		public void endSending(long size) {
		}

		@Override
		public void exceptionOccurred(Exception e) {
			System.out.println("Error occured sending file: "
					+ e.getClass().getSimpleName() + ": " + e.getMessage());
		}

		@Override
		public void sendingCanceled() {
		}

	}

	public static void main(String[] args) {
		try {
			BrowserImpl impl = new BrowserImpl("/home/sebastian/temp");
			impl.fireThumbnails(new IThumbnailListener() {
				@Override
				@Oneway
				public void setThumbnail(String file, int w, int h,
						int[] thumbnail) throws RemoteException {
					System.out.println(thumbnail.length);
				}
			}, 10, 10);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public ServerPort publishAbsoluteFile(String file) throws RemoteException,
			IOException {
		FileSender sender = new FileSender(new File(file), DOWNLOAD_PORT, 1);
		sender.getProgressListener().add(new BrowserSendListener());
		sender.sendAsync();
		ServerPort serverport = new ServerPort(Server.getServer()
				.getServerPort().getIp(), DOWNLOAD_PORT);
		return serverport;
	}

	@Override
	public void onThumbnailCalculation(ThumbnailJob job) {
		if (job instanceof BrowserThumbnailJob) {
			BrowserThumbnailJob browserJob = (BrowserThumbnailJob) job;
			if (currentFiles.contains(browserJob.imageFile.getAbsolutePath()))
				try {
					browserJob.listener.setThumbnail(browserJob.fileName,
							job.thumbnail.width, job.thumbnail.height,
							job.thumbnail.rgb);
				} catch (RemoteException e) {
				}
		}
	}

	private static class BrowserThumbnailJob extends ImageThumbnailJob {

		private IThumbnailListener listener;
		private String fileName;

		public BrowserThumbnailJob(File imageFile, int width, int height,
				IThumbnailListener listener, String fileName) {
			super(imageFile, width, height);
			this.listener = listener;
			this.fileName = fileName;
		}

	}

}
