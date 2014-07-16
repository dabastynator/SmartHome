package de.neo.remote.mobile.services;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.widget.Toast;
import de.neo.remote.controlcenter.api.IControlCenter;
import de.neo.remote.gpiopower.api.IInternetSwitch;
import de.neo.remote.mediaserver.api.IBrowser;
import de.neo.remote.mediaserver.api.IChatServer;
import de.neo.remote.mediaserver.api.IMediaServer;
import de.neo.remote.mediaserver.api.PlayingBean;
import de.neo.remote.mobile.services.RemoteService.IRemoteActionListener;
import de.neo.remote.mobile.services.RemoteService.PlayerListener;
import de.neo.remote.mobile.services.RemoteService.StationStuff;
import de.neo.remote.mobile.tasks.DownloadTask;
import de.neo.remote.mobile.util.BufferBrowser;
import de.neo.rmi.api.Server;
import de.neo.rmi.protokol.RemoteException;
import de.neo.rmi.protokol.ServerPort;
import de.neo.rmi.transceiver.AbstractReceiver;
import de.neo.rmi.transceiver.FileReceiver;
import de.neo.rmi.transceiver.FileSender;

/**
 * binder as api for this service. it provides all functionality to the remote
 * server.
 * 
 * @author sebastian
 */
public class PlayerBinder extends Binder {

	/**
	 * port for uploading files
	 */
	public static final int UPLOAD_PORT = 5034;

	/**
	 * the service of this binder
	 */
	public RemoteService service;

	/**
	 * current receiver
	 */
	public AbstractReceiver receiver;

	/**
	 * allocate new binder.
	 * 
	 * @param service
	 */
	public PlayerBinder(RemoteService service) {
		this.service = service;
	}

	public IControlCenter getControlCenter() {
		return service.controlCenter;
	}

	/**
	 * connect to server, ip of the server will be load from the database
	 * 
	 * @param id
	 * @param r
	 */
	public void connectToServer(final int id) {
		service.connectToServer(id);
	}

	/**
	 * get the remote chat server object
	 * 
	 * @return chat server
	 */
	public IChatServer getChatServer() {
		return service.chatServer;
	}

	/**
	 * @return true if there is a connection wich a server
	 */
	public boolean isConnected() {
		return service.controlCenter != null;
	}

	/**
	 * @return name of connected server
	 */
	public String getServerName() {
		return service.serverName;
	}

	public PlayerListener getPlayerListener() {
		return service.playerListener;
	}

	/**
	 * add new remote action listener. the listener will be informed about
	 * actions on this service
	 * 
	 * @param listener
	 */
	public void addRemoteActionListener(IRemoteActionListener listener) {
		if (!service.actionListener.contains(listener))
			service.actionListener.add(listener);
	}

	/**
	 * remove action listener
	 * 
	 * @param listener
	 */
	public void removeRemoteActionListener(IRemoteActionListener listener) {
		service.actionListener.remove(listener);
	}

	/**
	 * @return current playing file
	 */
	public PlayingBean getPlayingFile() {
		return service.playingFile;
	}

	/**
	 * download the given file from the remote browser
	 * 
	 * @param file
	 */
	public void downloadFile(IBrowser browser, String file) {
		new DownloadTask(browser, file, null, getServerName(), this).execute();
		service.notificationHandler.setFile(file);
	}

	/**
	 * download the given directory from the remote browser
	 * 
	 * @param directory
	 */
	public void downloadDirectory(IBrowser browser, String directory) {
		new DownloadTask(browser, null, directory, getServerName(), this)
				.execute();
		service.notificationHandler.setFile(directory);
	}

	/**
	 * get the current receiver
	 */
	public AbstractReceiver getReceiver() {
		return receiver;
	}

	/**
	 * @return local server
	 */
	public Server getServer() {
		return service.getServer();
	}

	/**
	 * upload of given file to connected server at current location
	 * 
	 * @param file
	 */
	public void uploadFile(final IBrowser browser, final File file) {
		try {
			FileSender fileSender = new FileSender(file, UPLOAD_PORT, 1, DownloadTask.PROGRESS_STEP);
			fileSender.getProgressListener().add(service.uploadListener);
			fileSender.setBufferSize(DownloadTask.BUFFER_SIZE);
			fileSender.sendAsync();
			new Thread() {
				public void run() {
					try {
						browser.updloadFile(file.getName(), Server.getServer()
								.getServerPort().getIp(), UPLOAD_PORT);
					} catch (Exception e) {
						showToastFromThread(
								"upload remote error: " + e.getMessage(),
								Toast.LENGTH_LONG);
					}
				};
			}.start();
			Toast.makeText(service, "upload file started", Toast.LENGTH_SHORT)
					.show();
		} catch (IOException e) {
			Toast.makeText(service, "upload error: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	private void showToastFromThread(final String message, final int length) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				Toast.makeText(service, message, length).show();
			}
		};
		service.handler.post(runnable);
	}

	public Map<String, Object> getUnits() {
		return service.unitMap;
	}

	public void disconnect() {
		service.disconnectFromServer();
	}

	public void refreshControlCenter() {
		service.refreshControlCenter();
	}

	public StationStuff getMediaServerByName(String mediaServerName)
			throws RemoteException {
		Object object = service.unitMap.get(mediaServerName);
		if (object instanceof IMediaServer) {
			IMediaServer mediaServer = (IMediaServer) object;
			StationStuff mediaObjects = null;
			if (service.stationStuff.containsKey(mediaServerName)) {
				mediaObjects = service.stationStuff.get(mediaServerName);
			} else {
				mediaObjects = new StationStuff();
				mediaObjects.browser = new BufferBrowser(
						mediaServer.createBrowser());
				mediaObjects.control = mediaServer.getControl();
				mediaObjects.mplayer = mediaServer.getMPlayer();
				mediaObjects.omxplayer = mediaServer.getOMXPlayer();
				mediaObjects.player = mediaObjects.mplayer;
				mediaObjects.pls = mediaServer.getPlayList();
				mediaObjects.totem = mediaServer.getTotemPlayer();
				mediaObjects.imageViewer = mediaServer.getImageViewer();
				mediaObjects.name = mediaServerName;
				service.stationStuff.put(mediaServerName, mediaObjects);
			}
			service.setCurrentMediaServer(mediaObjects);
			return mediaObjects;
		}
		return null;
	}

	public StationStuff getLatestMediaServer() {
		return service.currentMediaServer;
	}

	public float[] getUnitPosition(String name) {
		return service.unitMapPostion.get(name);
	}

	public Map<String, IInternetSwitch> getPower() {
		Map<String, IInternetSwitch> power = new HashMap<String, IInternetSwitch>();
		if (getUnits() == null)
			return power;
		for (String name : getUnits().keySet()) {
			Object object = getUnits().get(name);
			if (object instanceof IInternetSwitch) {
				power.put(name, (IInternetSwitch) object);
			}
		}
		return power;
	}

	public String getUnitDescription(String name) {
		return service.unitMapDescription.get(name);
	}

	public void downloadPlaylist(final IBrowser browser,
			final String[] playlist, final String name) {
		AsyncTask<String, Integer, Exception> downloader = new AsyncTask<String, Integer, Exception>() {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				Toast.makeText(service, "download started", Toast.LENGTH_SHORT)
						.show();
			}

			@Override
			protected Exception doInBackground(String... playlist) {
				try {
					String folder = Environment.getExternalStorageDirectory()
							.toString()
							+ File.separator
							+ getServerName().trim();
					File base = new File(folder);
					if (!base.exists())
						base.mkdir();
					String plsDir = folder + File.separator + name;
					if (!new File(plsDir).exists())
						new File(plsDir).mkdir();
					for (int i = 0; i < playlist.length; i++) {
						ServerPort serverport = browser
								.publishAbsoluteFile(playlist[i]);
						String[] split = playlist[i].split(File.separator);
						String itemName = split[split.length - 1];
						File newFile = new File(plsDir + File.separator
								+ itemName);
						FileReceiver receiver = new FileReceiver(
								serverport.getIp(), serverport.getPort(),
								200000, newFile);
						// set maximum byte size to 1MB
						receiver.setBufferSize(1000000);
						receiver.getProgressListener().add(
								service.downloadListener);
						service.notificationHandler.setFile(itemName);
						receiver.receiveSync();
					}
				} catch (Exception e) {
					return e;
				}
				return null;
			}
		};
		downloader.execute(playlist);
	}
}