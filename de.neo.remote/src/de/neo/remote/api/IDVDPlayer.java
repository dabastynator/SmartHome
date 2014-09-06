package de.neo.remote.api;

import de.neo.rmi.protokol.RemoteException;

/**
 * The DVD Player extends the Player functionality to play DVDs.
 * 
 * @author sebastian
 */
public interface IDVDPlayer extends IPlayer {

	/**
	 * Play dvd. Throws PlayerException if loading fails.
	 * 
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	void playDVD() throws RemoteException, PlayerException;

	/**
	 * Show the main menu of the dvd.
	 * 
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	void showMenu() throws RemoteException, PlayerException;

	/**
	 * Move cursor up in the dvd menu.
	 * 
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	void menuUp() throws RemoteException, PlayerException;

	/**
	 * Move cursor down in the dvd menu.
	 * 
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	void menuDown() throws RemoteException, PlayerException;

	/**
	 * Move cursor left in the dvd menu.
	 * 
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	void menuLeft() throws RemoteException, PlayerException;

	/**
	 * Move cursor right in the dvd menu.
	 * 
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	void menuRight() throws RemoteException, PlayerException;

	/**
	 * Press enter on current menu selection.
	 * 
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	void menuEnter() throws RemoteException, PlayerException;

	/**
	 * Press previous dvd button.
	 * 
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	void menuPrevious() throws RemoteException, PlayerException;

	/**
	 * Disable all subtitles.
	 * 
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	public void subtitleRemove() throws RemoteException, PlayerException;

	/**
	 * Show next subtitle.
	 * 
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	public void subtitleNext() throws RemoteException, PlayerException;

	/**
	 * Eject the dvd.
	 * 
	 * @throws RemoteException
	 */
	public void ejectDVD() throws RemoteException;
}
