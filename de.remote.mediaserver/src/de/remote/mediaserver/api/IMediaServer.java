package de.remote.mediaserver.api;

import de.newsystem.rmi.protokol.RemoteAble;
import de.newsystem.rmi.protokol.RemoteException;

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

}
