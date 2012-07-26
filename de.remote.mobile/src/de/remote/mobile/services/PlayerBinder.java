package de.remote.mobile.services;

import java.io.File;

import android.os.Binder;
import android.os.Environment;
import android.widget.Toast;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.protokol.RemoteException;
import de.newsystem.rmi.transceiver.AbstractReceiver;
import de.newsystem.rmi.transceiver.DirectoryReceiver;
import de.newsystem.rmi.transceiver.FileReceiver;
import de.remote.api.IBrowser;
import de.remote.api.IControl;
import de.remote.api.IPlayList;
import de.remote.api.IPlayer;
import de.remote.api.PlayingBean;
import de.remote.mobile.services.RemoteService.IRemoteActionListener;
import de.remote.mobile.services.RemoteService.PlayerListener;

/**
 * binder as api for this service. it provides all functionality to the remote
 * server.
 * 
 * @author sebastian
 */
public class PlayerBinder extends Binder {

	/**
	 * the service of this binder
	 */
	private RemoteBaseService service;

	/**
	 * current receiver
	 */
	private AbstractReceiver receiver;

	/**
	 * allocate new binder.
	 * 
	 * @param service
	 */
	public PlayerBinder(RemoteBaseService service) {
		this.service = service;
	}

	/**
	 * get remote browser object
	 * 
	 * @return browser
	 */
	public IBrowser getBrowser() {
		return service.browser;
	}

	/**
	 * get current remote player object
	 * 
	 * @return player
	 */
	public IPlayer getPlayer() {
		return service.player;
	}

	/**
	 * get remote control object
	 * 
	 * @return control
	 */
	public IControl getControl() {
		return service.control;
	}

	/**
	 * connect to server, ip of the server will be load from the database
	 * 
	 * @param id
	 * @param r
	 */
	public void connectToServer(final int id, final Runnable r) {
		new Thread() {
			public void run() {
				if (id == service.serverID) {
					if (r != null)
						r.run();
				} else {
					service.disconnect();
					service.serverID = id;
					service.serverIP = service.serverDB.getIpOfServer(id);
					service.serverName = service.serverDB.getNameOfServer(id);
					service.connect(r);
				}
			}
		}.start();
	}

	public void disconnect(final Runnable r) {
		new Thread() {
			public void run() {
				service.disconnect();
				if (r != null)
					service.handler.post(r);
			}
		}.start();
	}

	/**
	 * set mplayer for current player
	 * 
	 */
	public void useMPlayer() {
		new Thread() {
			public void run() {
				try {
					service.player = service.station.getMPlayer();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * set totem for current player
	 */
	public void useTotemPlayer() {
		new Thread() {
			public void run() {
				try {
					service.player = service.station.getTotemPlayer();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * get remote playlist object
	 * 
	 * @return playlist
	 */
	public IPlayList getPlayList() {
		return service.playList;
	}

	/**
	 * @return true if there is a connection wich a server
	 */
	public boolean isConnected() {
		return service.station != null;
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
	public void downloadFile(String file) {
		try {
			String ip = service.browser.publishFile(file,
					RemoteBaseService.DOWNLOAD_PORT);
			String folder = Environment.getExternalStorageDirectory()
					.toString() + File.separator + getServerName().trim();
			File dir = new File(folder);
			if (!dir.exists())
				dir.mkdir();
			File newFile = new File(folder + File.separator + file.trim());
			FileReceiver receiver = new FileReceiver(ip,
					RemoteBaseService.DOWNLOAD_PORT, 200000, newFile);
			service.progressListener.setFile(file);
			download(receiver);
		} catch (Exception e) {
			Toast.makeText(service, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * download the given directory from the remote browser
	 * 
	 * @param directory
	 */
	public void downloadDirectory(String directory) {
		try {
			String ip = service.browser.publishDirectory(directory,
					RemoteBaseService.DOWNLOAD_PORT);
			String folder = Environment.getExternalStorageDirectory()
					.toString() + File.separator + getServerName().trim();
			File dir = new File(folder);
			if (!dir.exists())
				dir.mkdir();
			DirectoryReceiver receiver = new DirectoryReceiver(ip,
					RemoteBaseService.DOWNLOAD_PORT, dir);
			service.progressListener.setFile(directory);
			download(receiver);
		} catch (Exception e) {
			Toast.makeText(service, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * configure receiver and start the download
	 * 
	 * @param receiver
	 */
	private void download(FileReceiver receiver) {
		this.receiver = receiver;
		// set maximum byte size to 1MB
		receiver.setBufferSize(1000000);
		receiver.getProgressListener().add(service.progressListener);
		receiver.receiveAsync();
		Toast.makeText(service, "download started", Toast.LENGTH_SHORT).show();
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
}