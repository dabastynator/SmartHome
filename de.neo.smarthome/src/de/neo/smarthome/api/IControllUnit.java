package de.neo.smarthome.api;

import de.neo.remote.rmi.RemoteAble;
import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.controlcenter.ControlCenter;

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
	 * Get ID of this unit.
	 * 
	 * @return id
	 * @throws RemoteException
	 */
	public String getID() throws RemoteException;

	/**
	 * Set the current control center to the unit.
	 * 
	 * @param controlCenter
	 */
	public void setControlCenter(ControlCenter controlCenter);

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
