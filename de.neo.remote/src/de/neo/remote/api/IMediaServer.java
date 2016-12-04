package de.neo.remote.api;

import de.neo.rmi.protokol.RemoteAble;
import de.neo.rmi.protokol.RemoteException;

/**
 * this music station holds all objects for the music control
 * 
 * @author sebastian
 */
public interface IMediaServer extends RemoteAble {

	/**
	 * standard port for the server
	 */
	public static final int STATION_PORT = 5006;

	/**
	 * create new browser for the file system
	 * 
	 * @return browser
	 * @throws RemoteException
	 */
	public IBrowser createBrowser() throws RemoteException;

	/**
	 * get totem player
	 * 
	 * @return player
	 * @throws RemoteException
	 */
	public IPlayer getTotemPlayer() throws RemoteException;

	/**
	 * get the omxplayer, just on raspberry
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public IPlayer getOMXPlayer() throws RemoteException;

	/**
	 * get mplayer
	 * 
	 * @return player
	 * @throws RemoteException
	 */
	public IDVDPlayer getMPlayer() throws RemoteException;

	/**
	 * get the object to control functions of the computer
	 * 
	 * @return control
	 * @throws RemoteException
	 */
	public IControl getControl() throws RemoteException;

	/**
	 * get the object to handle playlists
	 * 
	 * @return playlist
	 * @throws RemoteException
	 */
	public IPlayList getPlayList() throws RemoteException;

	public String[] listDirectories(String path) throws RemoteException;

	public String[] listFiles(String path) throws RemoteException;

	public String getBrowserPath();

}
