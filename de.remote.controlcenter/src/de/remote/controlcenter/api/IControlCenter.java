package de.remote.controlcenter.api;

import de.newsystem.rmi.protokol.RemoteAble;
import de.newsystem.rmi.protokol.RemoteException;

/**
 * The control center handles all control units and information about the
 * controlled object.
 * 
 * @author sebastian
 * 
 */
public interface IControlCenter extends RemoteAble {

	/**
	 * id of the music station handler object
	 */
	public static final String ID = "de.newsystem.controlcenter";

	/**
	 * Port of the server
	 */
	public static final int PORT = 5021;

	/**
	 * Get number of registered control units
	 * 
	 * @return number of control units
	 * @throws RemoteException
	 */
	public int getControlUnitNumber() throws RemoteException;

	/**
	 * Add new remote control unit.
	 * 
	 * @param controlUnit
	 * @throws RemoteException
	 */
	public void addControlUnit(IControlUnit controlUnit) throws RemoteException;

	/**
	 * Get the ground plot of the control center area.
	 * 
	 * @return ground plot
	 * @throws RemoteException
	 */
	public GroundPlot getGroundPlot() throws RemoteException;

	/**
	 * remove remote control unit.
	 * 
	 * @param controlUnit
	 * @throws RemoteException
	 */
	public void removeControlUnit(IControlUnit controlUnit)
			throws RemoteException;

	/**
	 * Get control unit by specified number.
	 * 
	 * @param number
	 * @return control unit by number
	 * @throws RemoteException
	 */
	public IControlUnit getControlUnit(int number) throws RemoteException;
}
