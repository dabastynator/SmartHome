package de.neo.smarthome.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.neo.persist.DaoException;
import de.neo.remote.rmi.RemoteAble;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebField;
import de.neo.remote.web.WebGet;
import de.neo.remote.web.WebRequest;

public interface IWebTrigger extends RemoteAble {

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
	public EventRule createEventRule(@WebGet(name = "token") String token, @WebGet(name = "trigger") String triggerID)
			throws RemoteException, DaoException;

	/**
	 * Delete event rule by given trigger id.
	 * 
	 * @param triggerID
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "delete_event_rule", description = "Delete event rule by given trigger id.")
	public void deleteEventRule(@WebGet(name = "token") String token, @WebGet(name = "trigger") String triggerID)
			throws RemoteException, DaoException;

	/**
	 * Create new event for given event rule. The event corresponds to a specifig
	 * unit and can have an optional condition.
	 * 
	 * @param triggerID
	 * @param unitID
	 * @return extended event rule
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "create_event_for_rule", description = "Create new event for given event rule. The event corresponds to a specifig unit and can have an optional condition.")
	public EventRule createEventForRule(@WebGet(name = "token") String token,
			@WebGet(name = "trigger") String triggerID, @WebGet(name = "unit") String unitID,
			@WebGet(name = "condition") String condition) throws RemoteException, DaoException;

	/**
	 * Delete event int given event rule by event index.
	 * 
	 * @param triggerID
	 * @param index
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "delete_event_in_rule", description = "Delete event in given event rule by event index.")
	public void deleteEventInRule(@WebGet(name = "token") String token, @WebGet(name = "trigger") String triggerID,
			@WebGet(name = "index") int index) throws RemoteException, DaoException;

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
	public EventRule addParameterforEventInRule(@WebGet(name = "token") String token,
			@WebGet(name = "trigger") String triggerID, @WebGet(name = "index") int index,
			@WebGet(name = "key") String key, @WebGet(name = "value") String value)
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
	public EventRule deleteParameterforEventInRule(@WebGet(name = "token") String token,
			@WebGet(name = "trigger") String triggerID, @WebGet(name = "event_index") int eventIndex,
			@WebGet(name = "parameter_index") int parameterIndex) throws RemoteException, DaoException;

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
	public EventRule setInformationsforEventRule(@WebGet(name = "token") String token,
			@WebGet(name = "trigger") String triggerID, @WebGet(name = "informations") String informations)
			throws RemoteException, DaoException;

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
	public EventRule setConditionforEvent(@WebGet(name = "token") String token,
			@WebGet(name = "trigger") String triggerID, @WebGet(name = "event_index") int eventIndex,
			@WebGet(name = "condition") String condition) throws RemoteException, DaoException;

	/**
	 * List all crone-job time trigger
	 * 
	 * @param token
	 * @return list of time trigger
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "list_timetrigger", description = "List all crone-job time trigger.", genericClass = TimeTriggerBean.class)
	public ArrayList<TimeTriggerBean> getTimeTrigger(@WebGet(name = "token") String token)
			throws RemoteException, DaoException;

	/**
	 * Create new crone-job time trigger
	 * 
	 * @param token
	 * @param triggerId
	 * @param cronJob
	 * @return new time trigger
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "create_timetrigger", description = "Create new crone-job time trigger.")
	public TimeTriggerBean createTimeTrigger(@WebGet(name = "token") String token,
			@WebGet(name = "trigger_id") String triggerId, @WebGet(name = "cron_job") String cronJob)
			throws RemoteException, DaoException;

	/**
	 * Delete crone-job time trigger for trigger id
	 * 
	 * @param token
	 * @param triggerId
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "delete_timetrigger", description = "Delete crone-job time trigger for trigger id.")
	public void deleteTimeTrigger(@WebGet(name = "token") String token, @WebGet(name = "id") long id)
			throws RemoteException, DaoException;

	public static class TimeTriggerBean implements Serializable {

		@WebField(name = "id")
		public long Id;
		
		@WebField(name = "trigger_id")
		public String TriggerId;

		@WebField(name = "crone_job")
		public String CroneJob;

	}
}
