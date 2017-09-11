package de.neo.smarthome.controlcenter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.neo.persist.DaoException;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebField;
import de.neo.remote.web.WebGet;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.api.GroundPlot;
import de.neo.smarthome.api.Trigger;

/**
 * The control center handles all control units and information about the
 * controlled object.
 * 
 * @author sebastian
 * 
 */
public interface IControlCenter {

	/**
	 * Add new remote control unit.
	 * 
	 * @param controlUnit
	 * @throws RemoteException
	 */
	public void addControlUnit(IControllUnit controlUnit) throws RemoteException;

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
	public void removeControlUnit(IControllUnit controlUnit) throws RemoteException;

	/**
	 * Get control unit by specified unit-id.
	 * 
	 * @param id
	 * @return control unit by number
	 * @throws RemoteException
	 */
	public IControllUnit getControlUnit(String id) throws RemoteException;

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
	 * @param triggerID
	 * @return map contains result
	 * @throws RemoteException
	 */
	@WebRequest(description = "Perform specified trigger", path = "dotrigger", genericClass = Integer.class)
	public HashMap<String, Integer> performTrigger(@WebGet(name = "trigger") String triggerID) throws RemoteException;

	/**
	 * List all event-rules of the controlcenter. A rule can be triggered by the
	 * speicified trigger.
	 * 
	 * @return list of rules
	 * @throws RemoteException
	 */
	@WebRequest(path = "rules", description = "List all event-rules of the controlcenter. A rule can be triggered by the speicified trigger.")
	public List<EventRule> getEvents() throws RemoteException;

	/**
	 * Create new event rule for given trigger id.
	 * 
	 * @param triggerID
	 * @return new event rule
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "create_event_rule", description = "Create new event rule for given trigger id.")
	public EventRule createEventRule(@WebGet(name = "trigger") String triggerID) throws RemoteException, DaoException;

	/**
	 * Delete event rule by given trigger id.
	 * 
	 * @param triggerID
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "delete_event_rule", description = "Delete event rule by given trigger id.")
	public void deleteEventRule(@WebGet(name = "trigger") String triggerID) throws RemoteException, DaoException;

	/**
	 * Create new event for given event rule. The event corresponds to a
	 * specifig unit and can have an optional condition.
	 * 
	 * @param triggerID
	 * @param unitID
	 * @return extended event rule
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "create_event_for_rule", description = "Create new event for given event rule. The event corresponds to a specifig unit and can have an optional condition.")
	public EventRule createEventForRule(@WebGet(name = "trigger") String triggerID,
			@WebGet(name = "unit") String unitID, @WebGet(name = "condition") String condition)
			throws RemoteException, DaoException;

	/**
	 * Delete event int given event rule by event index.
	 * 
	 * @param triggerID
	 * @param index
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "delete_event_in_rule", description = "Delete event in given event rule by event index.")
	public void deleteEventInRule(@WebGet(name = "trigger") String triggerID, @WebGet(name = "index") int index)
			throws RemoteException, DaoException;

	/**
	 * Add parameter for event given event rule by event index.
	 * 
	 * @param triggerID
	 * @param index
	 * @param key
	 * @param value
	 * @return modified event rule
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "add_parameter_for_event", description = "Add parameter for event given event rule by event index.")
	public EventRule addParameterforEventInRule(@WebGet(name = "trigger") String triggerID,
			@WebGet(name = "index") int index, @WebGet(name = "key") String key, @WebGet(name = "value") String value)
			throws RemoteException, DaoException;

	/**
	 * Delete parameter for event given event rule by event index and parameter
	 * index.
	 * 
	 * @param triggerID
	 * @param eventIndex
	 * @param parameterIndex
	 * @return modified event rule
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "delete_parameter_for_event", description = "Delete parameter for event given event rule by event index and parameter index.")
	public EventRule deleteParameterforEventInRule(@WebGet(name = "trigger") String triggerID,
			@WebGet(name = "event_index") int eventIndex, @WebGet(name = "parameter_index") int parameterIndex)
			throws RemoteException, DaoException;

	/**
	 * Set informations for event given event rule. Several information are
	 * separated by comma.
	 * 
	 * @param triggerID
	 * @param information
	 * @return modified event rule
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "set_information_for_event_rule", description = "Set informations for event given event rule. Several information are separated by comma.")
	public EventRule setInformationsforEventRule(@WebGet(name = "trigger") String triggerID,
			@WebGet(name = "informations") String informations) throws RemoteException, DaoException;

	/**
	 * Set condition for an event of an given event rule.
	 * 
	 * @param triggerID
	 * @param eventIndex
	 * @param constrains
	 * @return modified event rule
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "set_condition_for_event_in_rule", description = "Set condition for an event of an given event rule.")
	public EventRule setConditionforEvent(@WebGet(name = "trigger") String triggerID,
			@WebGet(name = "event_index") int eventIndex, @WebGet(name = "condition") String condition)
			throws RemoteException, DaoException;

	/**
	 * Get all controlunits of the controlcenter. The map maps the control-unit
	 * id to the object.
	 * 
	 * @return map of controlunits
	 */
	public Map<String, IControllUnit> getControlUnits();

	public static class BeanWeb implements Serializable {

		private static final long serialVersionUID = -4066506544238955935L;

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

		public void merge(BeanWeb webBean) {
			mID = webBean.getID();
			mName = webBean.getName();
			mDescription = webBean.getDescription();
			mX = webBean.getX();
			mY = webBean.getY();
			mZ = webBean.getZ();
		}

	}
}
