package de.remote.api;

import de.newsystem.rmi.protokol.RemoteAble;
import de.newsystem.rmi.protokol.RemoteException;

/**
 * The station handler handles severals music stations.
 * 
 * @author sebastian
 */
public interface IStationHandler extends RemoteAble{

	/**
	 * id of the music station handler object
	 */
	public static final String STATION_ID = "de.newsystem.musicstation.handler";
	
	/**
	 * Get the number of registered music stations.
	 * 
	 * @return number of stations
	 * @throws RemoteException
	 */
	public int getStationSize() throws RemoteException;

	/**
	 * Get specified music station.
	 * 
	 * @param station
	 * @return music station
	 * @throws RemoteException
	 */
	public IMusicStation getStation(int station) throws RemoteException;

	/**
	 * Add music station
	 * 
	 * @param station
	 * @throws RemoteException
	 */
	public void addMusicStation(IMusicStation station) throws RemoteException;

}
