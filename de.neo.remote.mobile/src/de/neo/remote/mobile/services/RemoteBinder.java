package de.neo.remote.mobile.services;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.widget.Toast;
import de.neo.remote.api.IBrowser;
import de.neo.remote.api.IControlCenter;
import de.neo.remote.api.IInternetSwitch;
import de.neo.remote.api.IMediaServer;
import de.neo.remote.api.PlayingBean;
import de.neo.remote.mobile.activities.AbstractConnectionActivity;
import de.neo.remote.mobile.persistence.RemoteServer;
import de.neo.remote.mobile.services.RemoteService.BufferdUnit;
import de.neo.remote.mobile.services.RemoteService.IRemoteActionListener;
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
public class RemoteBinder extends Binder {

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
	public RemoteBinder(RemoteService service) {
		this.service = service;
	}

	public IControlCenter getControlCenter() {
		return service.mCurrentControlCenter;
	}

	/**
	 * connect to server, ip of the server will be load from the database
	 * 
	 * @param id
	 * @param r
	 */
	public void connectToServer(RemoteServer server) {
		service.connectToServer(server);
	}

	/**
	 * @return true if there is a connection wich a server
	 */
	public boolean isConnected() {
		return service.mCurrentControlCenter != null;
	}

	/**
	 * add new remote action listener. the listener will be informed about
	 * actions on this service
	 * 
	 * @param listener
	 */
	public void addRemoteActionListener(IRemoteActionListener listener) {
		if (!service.mActionListener.contains(listener))
			service.mActionListener.add(listener);
	}

	/**
	 * remove action listener
	 * 
	 * @param listener
	 */
	public void removeRemoteActionListener(IRemoteActionListener listener) {
		service.mActionListener.remove(listener);
	}

	/**
	 * @return current playing file
	 */
	public PlayingBean getPlayingFile() {
		return service.mCurrentPlayingFile;
	}

	/**
	 * download the given file from the remote browser
	 * 
	 * @param file
	 */
	public void downloadFile(AbstractConnectionActivity activity,
			IBrowser browser, String file) {
		new DownloadTask(activity, browser, file, null,
				service.mCurrentServer.getName(), this).execute();
		service.mNotificationHandler.setFile(file);
	}

	/**
	 * download the given directory from the remote browser
	 * 
	 * @param directory
	 */
	public void downloadDirectory(AbstractConnectionActivity activity,
			IBrowser browser, String directory) {
		new DownloadTask(activity, browser, null, directory,
				service.mCurrentServer.getName(), this).execute();
		service.mNotificationHandler.setFile(directory);
	}

	/**
	 * get the current receiver
	 */
	public AbstractReceiver getReceiver() {
		return receiver;
	}

	/**
	 * upload of given file to connected server at current location
	 * 
	 * @param file
	 */
	public void uploadFile(final IBrowser browser, final File file) {
		try {
			FileSender fileSender = new FileSender(file, UPLOAD_PORT, 1,
					DownloadTask.PROGRESS_STEP);
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
		service.mHandler.post(runnable);
	}

	public Map<String, BufferdUnit> getUnits() {
		return service.mUnitMap;
	}

	public void disconnect() {
		service.disconnectFromServer();
	}

	public void refreshControlCenter() {
		service.refreshControlCenter();
	}

	public StationStuff getMediaServerByID(String id) throws RemoteException {
		BufferdUnit unit = service.mUnitMap.get(id);
		if (unit != null && unit.mObject instanceof IMediaServer) {
			IMediaServer mediaServer = (IMediaServer) unit.mObject;
			if (unit.mStation == null) {
				unit.mStation = new StationStuff();
				unit.mStation.browser = new BufferBrowser(
						mediaServer.createBrowser());
				unit.mStation.control = mediaServer.getControl();
				unit.mStation.mplayer = mediaServer.getMPlayer();
				unit.mStation.omxplayer = mediaServer.getOMXPlayer();
				unit.mStation.player = unit.mStation.mplayer;
				unit.mStation.pls = mediaServer.getPlayList();
				unit.mStation.totem = mediaServer.getTotemPlayer();
				unit.mStation.imageViewer = mediaServer.getImageViewer();
				unit.mStation.name = unit.mName;
			}
			service.setCurrentMediaServer(unit.mStation);
			return unit.mStation;
		}
		return null;
	}

	public StationStuff getLatestMediaServer() {
		return service.mCurrentMediaServer;
	}

	public Map<String, BufferdUnit> getSwitches() {
		Map<String, BufferdUnit> power = new HashMap<String, BufferdUnit>();
		if (getUnits() == null)
			return power;
		for (String id : getUnits().keySet()) {
			BufferdUnit unit = getUnits().get(id);
			if (unit.mObject instanceof IInternetSwitch) {
				power.put(id, unit);
			}
		}
		return power;
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
							+ service.mCurrentServer.getName();
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
						service.mNotificationHandler.setFile(itemName);
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

	public RemoteServer getServer() {
		return service.mCurrentServer;
	}
}