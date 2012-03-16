package de.remote.api;

import de.newsystem.rmi.protokol.RemoteException;

/**
 * this factory holds all objects for the computer control
 * 
 * @author sebastian
 */
public interface IStation {

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
	public IPlayer getMPlayer() throws RemoteException;

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

	/**
	 * get the chat object
	 * 
	 * @return chatServer
	 * @throws RemoteException
	 */
	public IChatServer getChatServer() throws RemoteException;
}
