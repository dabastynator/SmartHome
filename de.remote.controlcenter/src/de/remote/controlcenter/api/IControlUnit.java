package de.remote.controlcenter.api;

import de.newsystem.rmi.protokol.RemoteAble;
import de.newsystem.rmi.protokol.RemoteException;

/**
 * The control unit is a single remote able control unit.
 * 
 * @author sebastian
 */
public interface IControlUnit extends RemoteAble {

	/**
	 * @return name of control unit
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException;

	/**
	 * Get description of the control unit.
	 * 
	 * @return description of unit
	 * @throws RemoteException
	 */
	public String getDescription() throws RemoteException;

	/**
	 * Get the interface of the remote able control unit.
	 * 
	 * @return interface of the remote able object
	 * @throws RemoteException
	 */
	@SuppressWarnings("rawtypes")
	public Class getRemoteableControlInterface() throws RemoteException;

	/**
	 * Get the remote able control unit object
	 * 
	 * @return control unit object
	 * @throws RemoteException
	 */
	public Object getRemoteableControlObject() throws RemoteException;
}
