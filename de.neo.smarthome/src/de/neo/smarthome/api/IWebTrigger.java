package de.neo.smarthome.api;

import java.io.Serializable;
import java.text.ParseException;
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
	public HashMap<String, Integer> performTrigger(@WebGet(name = "token") String token,
			@WebGet(name = "trigger") String triggerID) throws RemoteException;

	/**
	 * // * List all scripts of the controlcenter. A script can be triggered by its
	 * trigger id.
	 * 
	 * @return list of scripts
	 * @throws RemoteException
	 */
	@WebRequest(path = "scripts", description = "List all scripts of the controlcenter. A script can be triggered by its trigger id.")
	public List<Script> getScripts(@WebGet(name = "token") String token) throws RemoteException;

	/**
	 * Create new script for given trigger id.
	 * 
	 * @param triggerID
	 * @return new script
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "create_script", description = "Create new script for given trigger id.")
	public Script createScript(@WebGet(name = "token") String token, @WebGet(name = "trigger") String triggerID)
			throws RemoteException, DaoException;

	/**
	 * Delete script by given trigger id.
	 * 
	 * @param triggerID
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "delete_script", description = "Delete script by given trigger id.")
	public void deleteScript(@WebGet(name = "token") String token, @WebGet(name = "trigger") String triggerID)
			throws RemoteException, DaoException;

	/**
	 * Create new event for given script. The event corresponds to a specific unit
	 * and can have an optional condition.
	 * 
	 * @param triggerID
	 * @param unitID
	 * @return extended script
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "create_event", description = "Create new event for given script. The event corresponds to a specific unit and can have an optional condition.")
	public Script createEvent(@WebGet(name = "token") String token, @WebGet(name = "trigger") String triggerID,
			@WebGet(name = "unit") String unitID, @WebGet(name = "condition") String condition)
			throws RemoteException, DaoException;

	/**
	 * Delete event in given script by event index.
	 * 
	 * @param triggerID
	 * @param index
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "delete_event", description = "Delete event in given script by event index.")
	public void deleteEvent(@WebGet(name = "token") String token, @WebGet(name = "trigger") String triggerID,
			@WebGet(name = "index") int index) throws RemoteException, DaoException;

	/**
	 * Add parameter for event given script by event index.
	 * 
	 * @param triggerID
	 * @param index
	 * @param key
	 * @param value
	 * @return modified script
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "add_parameter_for_event", description = "Add parameter for event given script by event index.")
	public Script addParameterforEvent(@WebGet(name = "token") String token, @WebGet(name = "trigger") String triggerID,
			@WebGet(name = "index") int index, @WebGet(name = "key") String key, @WebGet(name = "value") String value)
			throws RemoteException, DaoException;

	/**
	 * Delete parameter for event given script by event index and parameter index.
	 * 
	 * @param triggerID
	 * @param eventIndex
	 * @param parameterIndex
	 * @return modified script
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "delete_parameter_for_event", description = "Delete parameter for event given script by event index and parameter index.")
	public Script deleteParameterforEvent(@WebGet(name = "token") String token,
			@WebGet(name = "trigger") String triggerID, @WebGet(name = "event_index") int eventIndex,
			@WebGet(name = "parameter_index") int parameterIndex) throws RemoteException, DaoException;

	/**
	 * Set informations for event given script. Several information are separated by
	 * comma.
	 * 
	 * @param triggerID
	 * @param information
	 * @return modified script
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "set_information_for_script", description = "Set informations for event given script. Several information are separated by comma.")
	public Script setInformationsForScript(@WebGet(name = "token") String token,
			@WebGet(name = "trigger") String triggerID, @WebGet(name = "informations") String informations)
			throws RemoteException, DaoException;

	/**
	 * Set condition for an event of a given script.
	 * 
	 * @param triggerID
	 * @param eventIndex
	 * @param constrains
	 * @return modified script
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "set_condition_for_event", description = "Set condition for an event of an given script.")
	public Script setConditionForEvent(@WebGet(name = "token") String token, @WebGet(name = "trigger") String triggerID,
			@WebGet(name = "event_index") int eventIndex, @WebGet(name = "condition") String condition)
			throws RemoteException, DaoException;

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
	 * Disable/Enable crone-job time trigger
	 * 
	 * @param token
	 * @param id
	 * @param enabled
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "set_timetrigger_enabled", description = "Disable/Enable crone-job time trigger.")
	public void setTimeTriggerEnabled(@WebGet(name = "token") String token, @WebGet(name = "id") long id,
			@WebGet(name = "enabled") boolean enabled) throws RemoteException, DaoException;

	/**
	 * Change crone-job time trigger properties.
	 * 
	 * @param token
	 * @param id
	 * @param trigger
	 * @param cron
	 * @throws RemoteException
	 * @throws DaoException
	 * @throws ParseException 
	 */
	@WebRequest(path = "set_timetrigger_properties", description = "Change crone-job time trigger properties.")
	public void setTimeTriggerProperties(@WebGet(name = "token") String token, @WebGet(name = "id") long id,
			@WebGet(name = "trigger") String trigger, @WebGet(name = "cron") String cron)
			throws RemoteException, DaoException, ParseException;

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

		@WebField(name = "enabled")
		public boolean Enabled;

	}
}
