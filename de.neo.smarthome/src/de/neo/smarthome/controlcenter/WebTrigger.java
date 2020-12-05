package de.neo.smarthome.controlcenter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.neo.persist.Dao;
import de.neo.persist.DaoException;
import de.neo.persist.DaoFactory;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebGet;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.AbstractUnitHandler;
import de.neo.smarthome.SmartHome.ControlUnitFactory;
import de.neo.smarthome.api.Event;
import de.neo.smarthome.api.Script;
import de.neo.smarthome.api.IControlCenter;
import de.neo.smarthome.api.IWebTrigger;
import de.neo.smarthome.api.Trigger;
import de.neo.smarthome.api.IControlCenter.BeanWeb;
import de.neo.smarthome.api.IControllUnit;
import de.neo.smarthome.api.Trigger.Parameter;
import de.neo.smarthome.cronjob.CronJob;
import de.neo.smarthome.cronjob.CronScheduler;
import de.neo.smarthome.user.User.UserRole;
import de.neo.smarthome.user.UserSessionHandler;

public class WebTrigger extends AbstractUnitHandler implements IWebTrigger {

	public WebTrigger(IControlCenter center) {
		super(center);
	}

	@Override
	@WebRequest(description = "Perform specified trigger", path = "dotrigger", genericClass = Integer.class)
	public HashMap<String, Integer> performTrigger(@WebGet(name = "token") String token,
			@WebGet(name = "trigger") String triggerID) throws RemoteException {
		UserSessionHandler.require(token);
		Trigger trigger = new Trigger();
		trigger.setTriggerID(triggerID);
		int eventcount = mCenter.trigger(trigger);
		HashMap<String, Integer> result = new HashMap<>();
		result.put("triggered_events", eventcount);
		return result;
	}

	@Override
	@WebRequest(path = "scripts", description = "List all scripts of the controlcenter. A script can be triggered by its trigger id.")
	public List<Script> getScripts(@WebGet(name = "token") String token) throws RemoteException {
		UserSessionHandler.require(token);
		return mCenter.getScripts();
	}

	@Override
	@WebRequest(path = "create_script", description = "Create new script for given trigger id.")
	public Script createScript(@WebGet(name = "token") String token, @WebGet(name = "trigger") String triggerID)
			throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		Script script = new Script();
		script.setTriggerID(triggerID);
		script.setControlcenter(mCenter);
		mCenter.addScript(script);
		return script;
	}

	@Override
	@WebRequest(path = "delete_script", description = "Delete script by given trigger id.")
	public void deleteScript(@WebGet(name = "token") String token, @WebGet(name = "trigger") String triggerID)
			throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		mCenter.deleteScript(triggerID);
	}

	@Override
	@WebRequest(path = "create_event", description = "Create new event for given script. The event corresponds to a specific unit and can have an optional condition.")
	public Script createEvent(@WebGet(name = "token") String token, @WebGet(name = "trigger") String triggerID,
			@WebGet(name = "unit") String unitID, @WebGet(name = "condition") String condition)
			throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		Script script = mCenter.getScript(triggerID);
		if (script == null)
			throw new IllegalArgumentException("Script with trigger id '" + triggerID + "' does not exists!");
		Event event = new Event();
		event.setUnitID(unitID);
		event.setCondition(condition);
		script.getEvents().add(event);

		Dao<Event> eventDao = DaoFactory.getInstance().getDao(Event.class);
		eventDao.save(event);

		Dao<Script> scriptDao = DaoFactory.getInstance().getDao(Script.class);
		scriptDao.update(script);
		return script;
	}

	@Override
	@WebRequest(path = "delete_event", description = "Delete event in given script by event index.")
	public void deleteEvent(@WebGet(name = "token") String token, @WebGet(name = "trigger") String triggerID,
			@WebGet(name = "index") int index) throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		Script script = mCenter.getScript(triggerID);
		if (script == null)
			throw new IllegalArgumentException("Script with trigger id '" + triggerID + "' does not exists!");
		if (script.getEvents().size() <= index)
			throw new IllegalArgumentException(
					"Event index out of range. Script has " + script.getEvents().size() + " event(s).");
		Event event = script.getEvents().get(index);
		script.getEvents().remove(index);

		Dao<Event> eventDao = DaoFactory.getInstance().getDao(Event.class);
		eventDao.delete(event);

		Dao<Script> scriptDao = DaoFactory.getInstance().getDao(Script.class);
		scriptDao.update(script);
	}

	@Override
	@WebRequest(path = "add_parameter_for_event", description = "Add parameter for event given script by event index.")
	public Script addParameterforEvent(@WebGet(name = "token") String token, @WebGet(name = "trigger") String triggerID,
			@WebGet(name = "index") int index, @WebGet(name = "key") String key, @WebGet(name = "value") String value)
			throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		Script script = mCenter.getScript(triggerID);
		if (script == null)
			throw new IllegalArgumentException("Script with trigger id '" + triggerID + "' does not exists!");
		if (script.getEvents().size() <= index)
			throw new IllegalArgumentException(
					"Event index out of range. Script has " + script.getEvents().size() + " event(s).");
		Event event = script.getEvents().get(index);
		Parameter param = new Parameter();
		param.mKey = key;
		param.mValue = value;
		event.addParameter(param);

		Dao<Parameter> paramDao = DaoFactory.getInstance().getDao(Parameter.class);
		paramDao.save(param);

		Dao<Event> eventDao = DaoFactory.getInstance().getDao(Event.class);
		eventDao.update(event);
		return script;
	}

	@Override
	@WebRequest(path = "delete_parameter_for_event", description = "Delete parameter for event given Script by event index and parameter index.")
	public Script deleteParameterforEvent(@WebGet(name = "token") String token,
			@WebGet(name = "trigger") String triggerID, @WebGet(name = "event_index") int eventIndex,
			@WebGet(name = "parameter_index") int parameterIndex) throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		Script script = mCenter.getScript(triggerID);
		if (script == null)
			throw new IllegalArgumentException("Script with trigger id '" + triggerID + "' does not exists!");
		if (script.getEvents().size() <= eventIndex)
			throw new IllegalArgumentException(
					"Event index out of range. Script has " + script.getEvents().size() + " event(s).");
		Event event = script.getEvents().get(eventIndex);
		if (event.getParameter().size() <= parameterIndex)
			throw new IllegalArgumentException(
					"Parameter index out of range. Event has " + event.getParameter().size() + " parameter(s).");
		Parameter param = event.getParameteList().get(parameterIndex);
		event.deleteParameter(param);

		Dao<Parameter> paramDao = DaoFactory.getInstance().getDao(Parameter.class);
		paramDao.delete(param);

		Dao<Event> eventDao = DaoFactory.getInstance().getDao(Event.class);
		eventDao.update(event);
		return script;
	}

	@Override
	@WebRequest(path = "set_information_for_script", description = "Set informations for event given script. Several information are separated by comma.")
	public Script setInformationsForScript(@WebGet(name = "token") String token,
			@WebGet(name = "trigger") String triggerID, @WebGet(name = "informations") String informations)
			throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		Script script = mCenter.getScript(triggerID);
		if (script == null)
			throw new IllegalArgumentException("Script with trigger id '" + triggerID + "' does not exists!");
		script.setInformation(informations);

		Dao<Script> scriptDao = DaoFactory.getInstance().getDao(Script.class);
		scriptDao.update(script);

		return script;
	}

	@Override
	@WebRequest(path = "set_condition_for_event", description = "Set condition for an event of an given script.")
	public Script setConditionForEvent(@WebGet(name = "token") String token, @WebGet(name = "trigger") String triggerID,
			@WebGet(name = "event_index") int eventIndex, @WebGet(name = "condition") String condition)
			throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		Script script = mCenter.getScript(triggerID);
		if (script == null)
			throw new IllegalArgumentException("Script with trigger id '" + triggerID + "' does not exists!");
		if (script.getEvents().size() <= eventIndex)
			throw new IllegalArgumentException(
					"Event index out of range. Script has " + script.getEvents().size() + " event(s).");
		Event event = script.getEvents().get(eventIndex);
		event.setCondition(condition);

		Dao<Event> eventDao = DaoFactory.getInstance().getDao(Event.class);
		eventDao.update(event);
		return script;
	}

	private TimeTriggerBean toTTBean(CronJobTrigger trigger) {
		TimeTriggerBean triggerBean = new TimeTriggerBean();
		triggerBean.CroneJob = trigger.getCronDescription();
		triggerBean.Id = trigger.getId();
		triggerBean.Enabled = trigger.isEnabled();
		if (trigger.getTriggerList().size() > 0) {
			triggerBean.TriggerId = trigger.getTriggerList().get(0).getTriggerID();
		}
		return triggerBean;
	}

	@Override
	@WebRequest(path = "list_timetrigger", description = "List all crone-job time trigger.", genericClass = TimeTriggerBean.class)
	public ArrayList<TimeTriggerBean> getTimeTrigger(@WebGet(name = "token") String token)
			throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		ArrayList<TimeTriggerBean> list = new ArrayList<>();
		for (CronJobTrigger trigger : mCenter.getCronTriggers()) {
			list.add(toTTBean(trigger));
		}
		return list;
	}

	@Override
	@WebRequest(path = "create_timetrigger", description = "Create new crone-job time trigger.")
	public TimeTriggerBean createTimeTrigger(@WebGet(name = "token") String token,
			@WebGet(name = "trigger_id") String triggerId, @WebGet(name = "cron_job") String cronJob)
			throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		CronJobTrigger trigger = new CronJobTrigger();
		trigger.setControlCenter(mCenter);
		trigger.setCronDescription(cronJob);
		Trigger t = new Trigger();
		t.setTriggerID(triggerId);
		trigger.getTriggerList().add(t);
		mCenter.addCronTrigger(trigger);
		return toTTBean(trigger);
	}

	@WebRequest(path = "set_timetrigger_enabled", description = "Disable/Enable crone-job time trigger.")
	public void setTimeTriggerEnabled(@WebGet(name = "token") String token, @WebGet(name = "id") long id,
			@WebGet(name = "enabled") boolean enabled) throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		CronJobTrigger timeTrigger = mCenter.getCronTrigger(id);
		if (timeTrigger == null) {
			throw new RemoteException("Unknown timetrigger id: " + id);
		}
		if (timeTrigger.isEnabled() == enabled) {
			return;
		}
		timeTrigger.setEnabled(enabled);
		Dao<CronJobTrigger> ttDao = DaoFactory.getInstance().getDao(CronJobTrigger.class);
		ttDao.update(timeTrigger);
	}

	@WebRequest(path = "set_timetrigger_properties", description = "Change crone-job time trigger properties.")
	public void setTimeTriggerProperties(@WebGet(name = "token") String token, @WebGet(name = "id") long id,
			@WebGet(name = "trigger") String triggerId, @WebGet(name = "cron") String cron)
			throws RemoteException, DaoException, ParseException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		CronJobTrigger timeTrigger = mCenter.getCronTrigger(id);
		if (timeTrigger == null) {
			throw new RemoteException("Unknown timetrigger id: " + id);
		}
		Trigger trigger = null;
		if (timeTrigger.getTriggerList().size() == 1) {
			trigger = timeTrigger.getTriggerList().get(0);
			if (trigger.getTriggerID().equals(triggerId) && cron.equals(timeTrigger.getCronDescription())) {
				return;
			}
		} else {
			trigger = new Trigger();
			timeTrigger.getTriggerList().add(trigger);
		}
		// Parse the cron expression
		CronJob job = new CronJob(null);
		job.parseExpression(cron);

		timeTrigger.setCronDescription(cron);
		trigger.setTriggerID(triggerId);
		Dao<CronJobTrigger> ttDao = DaoFactory.getInstance().getDao(CronJobTrigger.class);
		ttDao.update(timeTrigger);
		CronScheduler.getInstance().remove(timeTrigger);
		CronScheduler.getInstance().scheduleJob(timeTrigger, cron);
	}

	@Override
	@WebRequest(path = "delete_timetrigger", description = "Delete crone-job time trigger for trigger id.")
	public void deleteTimeTrigger(@WebGet(name = "token") String token, @WebGet(name = "id") long id)
			throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		CronJobTrigger trigger = mCenter.getCronTrigger(id);
		if (trigger == null) {
			throw new RemoteException("Unknown timetrigger id " + id);
		}
		mCenter.deleteCronTrigger(trigger);
	}

	@WebRequest(path = "list_controlunits", description = "List all available controlunits.", genericClass = BeanWeb.class)
	public ArrayList<BeanWeb> listControlUnits(@WebGet(name = "token") String token)
			throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		ArrayList<BeanWeb> units = new ArrayList<>();
		for (IControllUnit unit : mCenter.getControlUnits().values()) {
			units.add(unit.getWebBean());
		}
		return units;
	}

	@Override
	public String getWebPath() {
		return "trigger";
	}

	public static class TriggerFactory implements ControlUnitFactory {

		@Override
		public Class<?> getUnitClass() {
			return Trigger.class;
		}

		@Override
		public AbstractUnitHandler createUnitHandler(ControlCenter center) {
			return new WebTrigger(center);
		}

	}

}
