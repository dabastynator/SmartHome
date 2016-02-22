package de.neo.remote.mobile.services;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
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
	public RemoteService mService;

	/**
	 * current receiver
	 */
	public AbstractReceiver mReceiver;

	/**
	 * allocate new binder.
	 * 
	 * @param service
	 */
	public RemoteBinder(RemoteService service) {
		this.mService = service;
	}

	public IControlCenter getControlCenter() {
		return mService.mCurrentControlCenter;
	}

	/**
	 * connect to server, ip of the server will be load from the database
	 * 
	 * @param id
	 * @param r
	 */
	public void connectToServer(RemoteServer server, Activity activity) {
		mService.connectToServer(server, activity);
	}

	/**
	 * @return true if there is a connection wich a server
	 */
	public boolean isConnected() {
		return mService.mCurrentControlCenter != null;
	}

	/**
	 * add new remote action listener. the listener will be informed about
	 * actions on this service
	 * 
	 * @param listener
	 */
	public void addRemoteActionListener(IRemoteActionListener listener) {
		if (!mService.mActionListener.contains(listener))
			mService.mActionListener.add(listener);
	}

	/**
	 * remove action listener
	 * 
	 * @param listener
	 */
	public void removeRemoteActionListener(IRemoteActionListener listener) {
		mService.mActionListener.remove(listener);
	}

	/**
	 * @return current playing file
	 */
	public PlayingBean getPlayingFile() {
		return mService.mCurrentPlayingFile;
	}

	/**
	 * download the given file from the remote browser
	 * 
	 * @param file
	 */
	public void downloadFile(AbstractConnectionActivity activity, IBrowser browser, String file) {
		new DownloadTask(activity, browser, file, null, mService.mCurrentServer.getName(), this).execute();
		mService.mNotificationHandler.setFile(file);
	}

	/**
	 * download the given directory from the remote browser
	 * 
	 * @param directory
	 */
	public void downloadDirectory(AbstractConnectionActivity activity, IBrowser browser, String directory) {
		new DownloadTask(activity, browser, null, directory, mService.mCurrentServer.getName(), this).execute();
		mService.mNotificationHandler.setFile(directory);
	}

	/**
	 * get the current receiver
	 */
	public AbstractReceiver getReceiver() {
		return mReceiver;
	}

	/**
	 * upload of given file to connected server at current location
	 * 
	 * @param file
	 */
	public void uploadFile(final IBrowser browser, final File file) {
		try {
			FileSender fileSender = new FileSender(file, UPLOAD_PORT, 1, DownloadTask.PROGRESS_STEP);
			fileSender.getProgressListener().add(mService.uploadListener);
			fileSender.setBufferSize(DownloadTask.BUFFER_SIZE);
			fileSender.sendAsync();
			new Thread() {
				public void run() {
					try {
						browser.updloadFile(file.getName(), Server.getServer().getServerPort().getIp(), UPLOAD_PORT);
					} catch (Exception e) {
						showToastFromThread("upload remote error: " + e.getMessage(), Toast.LENGTH_LONG);
					}
				};
			}.start();
			Toast.makeText(mService, "upload file started", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			Toast.makeText(mService, "upload error: " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	private void showToastFromThread(final String message, final int length) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				Toast.makeText(mService, message, length).show();
			}
		};
		mService.mHandler.post(runnable);
	}

	public Map<String, BufferdUnit> getUnits() {
		return mService.mUnitMap;
	}

	public void disconnect() {
		mService.disconnectFromServer();
	}

	public void refreshControlCenter() throws RemoteException {
		mService.refreshControlCenter();
	}

	public StationStuff getMediaServerByID(String id) throws RemoteException {
		BufferdUnit unit = mService.mUnitMap.get(id);
		if (unit != null && unit.mObject instanceof IMediaServer) {
			IMediaServer mediaServer = (IMediaServer) unit.mObject;
			if (unit.mStation == null) {
				unit.mStation = new StationStuff();
				unit.mStation.browser = new BufferBrowser(mediaServer.createBrowser());
				unit.mStation.control = mediaServer.getControl();
				unit.mStation.mplayer = mediaServer.getMPlayer();
				unit.mStation.omxplayer = mediaServer.getOMXPlayer();
				unit.mStation.player = unit.mStation.mplayer;
				unit.mStation.pls = mediaServer.getPlayList();
				unit.mStation.totem = mediaServer.getTotemPlayer();
				unit.mStation.name = unit.mName;
			}
			mService.setCurrentMediaServer(unit.mStation);
			return unit.mStation;
		}
		return null;
	}

	public StationStuff getLatestMediaServer() {
		return mService.mCurrentMediaServer;
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

	public void downloadPlaylist(final IBrowser browser, final String[] playlist, final String name) {
		AsyncTask<String, Integer, Exception> downloader = new AsyncTask<String, Integer, Exception>() {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				Toast.makeText(mService, "download started", Toast.LENGTH_SHORT).show();
			}

			@Override
			protected Exception doInBackground(String... playlist) {
				try {
					String folder = Environment.getExternalStorageDirectory().toString() + File.separator
							+ mService.mCurrentServer.getName();
					File base = new File(folder);
					if (!base.exists())
						base.mkdir();
					String plsDir = folder + File.separator + name;
					if (!new File(plsDir).exists())
						new File(plsDir).mkdir();
					for (int i = 0; i < playlist.length; i++) {
						ServerPort serverport = browser.publishAbsoluteFile(playlist[i]);
						String[] split = playlist[i].split(File.separator);
						String itemName = split[split.length - 1];
						File newFile = new File(plsDir + File.separator + itemName);
						FileReceiver receiver = new FileReceiver(serverport.getIp(), serverport.getPort(), 200000,
								newFile);
						// set maximum byte size to 1MB
						receiver.setBufferSize(1000000);
						receiver.getProgressListener().add(mService.downloadListener);
						mService.mNotificationHandler.setFile(itemName);
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
		return mService.mCurrentServer;
	}
}