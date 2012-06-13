package de.remote.api;

import de.newsystem.rmi.api.Oneway;
import de.newsystem.rmi.protokol.RemoteAble;
import de.newsystem.rmi.protokol.RemoteException;

/**
 * 
 * this interface defines handling with playlists
 * 
 * @author sebastian
 * 
 */
public interface IPlayList extends RemoteAble {

	/**
	 * get list of all playlist files
	 * 
	 * @return all playlists
	 * @throws RemoteException
	 */
	public String[] getPlayLists() throws RemoteException;

	/**
	 * create new playlist
	 * 
	 * @param name
	 * @throws RemoteException
	 */
	@Oneway
	public void addPlayList(String name) throws RemoteException;

	/**
	 * extend the playlist with this file. the file can be a single file or a
	 * folder. if the playlist does not exist, a playerexeption will be thrown
	 * 
	 * @param pls
	 * @param file
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	public void extendPlayList(String pls, String file) throws RemoteException,
			PlayerException;

	/**
	 * get list of all files in the given playlist
	 * 
	 * @param pls
	 * @return files
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	public String[] listContent(String pls) throws RemoteException,
			PlayerException;

	/**
	 * remove given playlist
	 * 
	 * @param pls
	 * @throws RemoteException
	 */
	@Oneway
	public void removePlayList(String pls) throws RemoteException;

	/**
	 * rename the playlist
	 * 
	 * @param oldPls
	 * @param newPls
	 * @throws RemoteException
	 */
	public void renamePlayList(String oldPls, String newPls)
			throws RemoteException;

	/**
	 * remove playlist entry from playlist
	 * 
	 * @param pls
	 * @param item
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	public void removeItem(String pls, String item) throws RemoteException,
			PlayerException;

	/**
	 * generate the pull path to the playlist file
	 * 
	 * @param pls
	 * @return full path to the playlist
	 * @throws RemoteException
	 */
	public String getPlaylistFullpath(String pls) throws RemoteException;
}
