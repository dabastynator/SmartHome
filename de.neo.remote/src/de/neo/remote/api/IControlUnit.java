package de.neo.remote.api;

import de.neo.rmi.protokol.RemoteAble;
import de.neo.rmi.protokol.RemoteException;

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
	 * Get the position of the control unit. the length of the array must be 3
	 * (x, y and z).
	 * 
	 * @return position of unit
	 * @throws RemoteException
	 */
	public float[] getPosition() throws RemoteException;

	/**
	 * Get ID of this unit.
	 * 
	 * @return id
	 * @throws RemoteException
	 */
	public String getID() throws RemoteException;

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

	/**
	 * Perform specified event on this control unit
	 * 
	 * @param event
	 * @return true if successfully performed event, false otherwise
	 * @throws RemoteException
	 */
	public boolean performEvent(Event event) throws RemoteException,
			EventException;

	/**
	 * The EventException specifies occurring exceptions during executing an
	 * event.
	 * 
	 * @author sebastian
	 *
	 */
	public static class EventException extends Exception {

		public EventException(String message) {
			super(message);
		}

		/**
		 * generated
		 */
		private static final long serialVersionUID = -1206040442786574653L;

	}
}
