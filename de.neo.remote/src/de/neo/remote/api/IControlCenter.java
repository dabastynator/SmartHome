package de.neo.remote.api;

import de.neo.rmi.protokol.RemoteAble;
import de.neo.rmi.protokol.RemoteException;

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
	public static final int PORT = 5022;

	/**
	 * Get array of all unit-ids.
	 * 
	 * @return ids
	 * @throws RemoteException
	 */
	public String[] getControlUnitIDs() throws RemoteException;

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
	 * Get control unit by specified unit-id.
	 * 
	 * @param id
	 * @return control unit by number
	 * @throws RemoteException
	 */
	public IControlUnit getControlUnit(String id) throws RemoteException;

	/**
	 * Trigger a trigger. The trigger is specified by the parameter. EventRules
	 * are used to map events for control unit.
	 * 
	 * @param trigger
	 * @throws RemoteException
	 */
	public void trigger(Trigger trigger) throws RemoteException;

	/**
	 * The IEventRule maps one event to several control-units.
	 * 
	 * @author sebastian
	 *
	 */
	interface IEventRule {

		/**
		 * Get control-unit-ids for the specified event
		 * 
		 * @param event
		 * @return control unit ids
		 */
		public Event[] getEventsForTrigger(Trigger trigger)
				throws RemoteException;

	}
}
