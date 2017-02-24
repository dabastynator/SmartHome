package de.neo.smarthome.api;

import de.neo.remote.rmi.RemoteAble;
import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.api.IControlCenter.BeanWeb;

/**
 * The control unit is a single remote able control unit.
 * 
 * @author sebastian
 */
public interface IControllUnit extends RemoteAble {

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
	 * Get the remote able control unit object
	 * 
	 * @return control unit object
	 * @throws RemoteException
	 */
	public Object getControllObject() throws RemoteException;

	/**
	 * Perform specified event on this control unit
	 * 
	 * @param event
	 * @return true if successfully performed event, false otherwise
	 * @throws RemoteException
	 */
	public boolean performEvent(Event event) throws RemoteException, EventException;

	/**
	 * Return beanweb for this control unit.
	 * 
	 * @return beanweb
	 * @throws RemoteException
	 */
	public BeanWeb getWebBean() throws RemoteException;

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
