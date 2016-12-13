package de.neo.remote.api;

import java.util.List;
import java.util.Map;

import de.neo.rmi.api.WebField;
import de.neo.rmi.api.WebGet;
import de.neo.rmi.api.WebRequest;
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
	@WebRequest(path = "groundplot", description = "Get the ground plot of the control center area.")
	public GroundPlot getGroundPlot() throws RemoteException;

	/**
	 * remove remote control unit.
	 * 
	 * @param controlUnit
	 * @throws RemoteException
	 */
	public void removeControlUnit(IControlUnit controlUnit) throws RemoteException;

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
	 * @return number of triggered events
	 * @throws RemoteException
	 */
	public int trigger(Trigger trigger) throws RemoteException;

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
		public Event[] getEventsForTrigger(Trigger trigger) throws RemoteException;

	}

	/**
	 * @param triggerID
	 * @return map contains result
	 */
	@WebRequest(description = "Perform specified trigger", path = "dotrigger", genericClass = Integer.class)
	public Map<String, Integer> performTrigger(@WebGet(name = "trigger") String triggerID);

	/**
	 * List all event-rules of the controlcenter. A rule can be triggered by the
	 * speicified trigger.
	 * 
	 * @return list of rules
	 */
	@WebRequest(path = "rules", description = "List all event-rules of the controlcenter. A rule can be triggered by the speicified trigger.")
	public List<IEventRule> getEvents();

	/**
	 * Get all controlunits of the controlcenter. The map maps the control-unit
	 * id to the object.
	 * 
	 * @return map of controlunits
	 */
	public Map<String, IControlUnit> getControlUnits();

	public static class BeanWeb {

		@WebField(name = "name")
		private String mName;

		@WebField(name = "description")
		private String mDescription;

		@WebField(name = "x")
		private float mX;

		@WebField(name = "y")
		private float mY;

		@WebField(name = "z")
		private float mZ;

		@WebField(name = "id")
		private String mID;

		public String getName() {
			return mName;
		}

		public void setName(String name) {
			mName = name;
		}

		public String getDescription() {
			return mDescription;
		}

		public void setDescription(String description) {
			mDescription = description;
		}

		public float getX() {
			return mX;
		}

		public void setX(float x) {
			mX = x;
		}

		public float getY() {
			return mY;
		}

		public void setY(float y) {
			mY = y;
		}

		public float getZ() {
			return mZ;
		}

		public void setZ(float z) {
			mZ = z;
		}

		public String getID() {
			return mID;
		}

		public void setID(String iD) {
			mID = iD;
		}

	}
}
