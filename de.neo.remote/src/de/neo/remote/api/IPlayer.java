package de.neo.remote.api;

import de.neo.rmi.api.Oneway;
import de.neo.rmi.protokol.RemoteAble;
import de.neo.rmi.protokol.RemoteException;

/**
 * control a music and video player
 * 
 * @author sebastian
 */
public interface IPlayer extends RemoteAble {

	/**
	 * play given file or location
	 * 
	 * @param file
	 * @throws RemoteException
	 */
	@Oneway
	void play(String file) throws RemoteException;

	/**
	 * make pause or continue
	 * 
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	void playPause() throws RemoteException, PlayerException;

	/**
	 * exit the player
	 * 
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	void quit() throws RemoteException, PlayerException;

	/**
	 * play next file
	 * 
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	void next() throws RemoteException, PlayerException;

	/**
	 * play previous file
	 * 
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	void previous() throws RemoteException, PlayerException;

	/**
	 * seek forwards in current playing file
	 * 
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	void seekForwards() throws RemoteException, PlayerException;

	/**
	 * seek backwards in current playing file
	 * 
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	void seekBackwards() throws RemoteException, PlayerException;

	/**
	 * volume up
	 * 
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	void volUp() throws RemoteException, PlayerException;

	/**
	 * set specified volume. The value must between 0 and 100.
	 * 
	 * @param file
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	void setVolume(int volume) throws RemoteException, PlayerException;

	/**
	 * volume down
	 * 
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	void volDown() throws RemoteException, PlayerException;

	/**
	 * Set the player to fullscreen if full is true
	 * 
	 * @param full
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	void fullScreen(boolean full) throws RemoteException, PlayerException;

	/**
	 * play next audio track
	 * 
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	void nextAudio() throws RemoteException, PlayerException;

	/**
	 * move window left
	 * 
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	void moveLeft() throws RemoteException, PlayerException;

	/**
	 * move window right
	 * 
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	void moveRight() throws RemoteException, PlayerException;

	/**
	 * set the player to play random order for the files.
	 * 
	 * @param shuffle
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	void useShuffle(boolean shuffle) throws RemoteException, PlayerException;

	/**
	 * get the current playing file. return null if nothing is playing.
	 * 
	 * @return current playing file
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	PlayingBean getPlayingBean() throws RemoteException;

	/**
	 * add listener for player states
	 * 
	 * @param listener
	 * @throws RemoteException
	 */
	@Oneway
	void addPlayerMessageListener(IPlayerListener listener) throws RemoteException;

	/**
	 * remove listener for player states
	 * 
	 * @param listener
	 * @throws RemoteException
	 */
	@Oneway
	void removePlayerMessageListener(IPlayerListener listener) throws RemoteException;

	/**
	 * play the given playlist
	 * 
	 * @param pls
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	void playPlayList(String pls) throws RemoteException, PlayerException;

	/**
	 * Set position of playing file in specified seconds.
	 * 
	 * @param second
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	void setPlayingPosition(int second) throws RemoteException, PlayerException;

	/**
	 * Play a stream from youtube by specified url.
	 * 
	 * @param url
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	void playFromYoutube(String url) throws RemoteException, PlayerException;

	/**
	 * @return current volume of the player between 0 and 100.
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	int getVolume() throws RemoteException, PlayerException;

}
