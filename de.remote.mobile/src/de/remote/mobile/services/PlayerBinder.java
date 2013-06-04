package de.remote.mobile.services;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import android.os.Binder;
import android.os.Environment;
import android.widget.Toast;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.protokol.RemoteException;
import de.newsystem.rmi.transceiver.AbstractReceiver;
import de.newsystem.rmi.transceiver.DirectoryReceiver;
import de.newsystem.rmi.transceiver.FileReceiver;
import de.newsystem.rmi.transceiver.FileSender;
import de.remote.api.IBrowser;
import de.remote.api.IChatServer;
import de.remote.api.IControl;
import de.remote.api.IMusicStation;
import de.remote.api.IPlayList;
import de.remote.api.IPlayer;
import de.remote.api.PlayerException;
import de.remote.api.PlayingBean;
import de.remote.gpiopower.api.IGPIOPower;
import de.remote.mobile.services.RemoteBaseService.StationStuff;
import de.remote.mobile.services.RemoteService.IRemoteActionListener;
import de.remote.mobile.services.RemoteService.PlayerListener;
import de.remote.mobile.util.BufferBrowser;

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
	private RemoteBaseService service;

	/**
	 * current receiver
	 */
	private AbstractReceiver receiver;

	private boolean usesMplayer;

	private String musicStationName;

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
	public void connectToServer(final int id) {
		new Thread() {
			public void run() {
				if (id != service.serverID) {
					service.disconnect();
					service.serverID = id;
					service.serverIP = service.serverDB.getIpOfServer(id);
					service.serverName = service.serverDB.getNameOfServer(id);
					service.connect();
				}
			}
		}.start();
	}

	/**
	 * set mplayer for current player
	 * 
	 */
	public void useMPlayer() {
		usesMplayer = true;
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
		usesMplayer = false;
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
		return service.stationList != null;
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
			service.notificationHandler.setFile(file);
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
			service.notificationHandler.setFile(directory);
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
		receiver.getProgressListener().add(service.downloadListener);
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

	/**
	 * upload of given file to connected server at current location
	 * 
	 * @param file
	 */
	public void uploadFile(File file) {
		try {
			FileSender fileSender = new FileSender(file, UPLOAD_PORT, 1);
			fileSender.sendAsync();
			service.browser.updloadFile(file.getName(), Server.getServer()
					.getServerPort().getIp(), UPLOAD_PORT);
			Toast.makeText(service, "upload started", Toast.LENGTH_SHORT)
					.show();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(service, "upload error: " + e.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (RemoteException e) {
			e.printStackTrace();
			Toast.makeText(service, "upload error: " + e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}
	}

	public IGPIOPower getPower() {
		return service.power;
	}

	public void setMusicStation(String stationName) {
		try {
			IMusicStation station = service.musicStations.get(stationName);
			if (service.station == station)
				return;
			musicStationName = stationName;
			service.station = station;
			StationStuff stuff = service.stationStuff.get(station);
			if (stuff != null) {
				service.browser = stuff.browser;
				service.player = stuff.player;
				service.control = stuff.control;
				service.playList = stuff.pls;
			} else {
				stuff = new StationStuff();
				service.browser = new BufferBrowser(station.createBrowser());
				service.player = station.getMPlayer();
				service.control = station.getControl();
				service.playList = station.getPlayList();
				stuff.browser = service.browser;
				stuff.player = service.player;
				stuff.control = service.control;
				stuff.pls = service.playList;
				service.stationStuff.put(station, stuff);
			}
			service.registerAndUpdate();
		} catch (RemoteException e) {
			System.err.println(e.getClass().getSimpleName() + ": "
					+ e.getMessage());
		} catch (PlayerException e) {
			System.err.println(e.getClass().getSimpleName() + ": "
					+ e.getMessage());
		}
	}

	public boolean usesMPlayer() {
		return usesMplayer;
	}

	public Map<String, IMusicStation> getMusicStations() {
		return service.musicStations;
	}

	public IMusicStation getMusicStation() {
		return service.station;
	}

	public String getMusicStationName() {
		return musicStationName;
	}
}