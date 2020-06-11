package de.neo.smarthome.controlcenter;

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
import de.neo.smarthome.api.EventRule;
import de.neo.smarthome.api.IControlCenter;
import de.neo.smarthome.api.IWebTrigger;
import de.neo.smarthome.api.Trigger;
import de.neo.smarthome.api.Trigger.Parameter;
import de.neo.smarthome.user.User.UserRole;
import de.neo.smarthome.user.UserSessionHandler;

public class WebTrigger extends AbstractUnitHandler implements IWebTrigger {

	public WebTrigger(IControlCenter center) {
		super(center);
	}

	@Override
	@WebRequest(description = "Perform specified trigger", path = "dotrigger", genericClass = Integer.class)
	public HashMap<String, Integer> performTrigger(@WebGet(name = "token") String token, @WebGet(name = "trigger") String triggerID) throws RemoteException {
		UserSessionHandler.require(token);
		Trigger trigger = new Trigger();
		trigger.setTriggerID(triggerID);
		int eventcount = mCenter.trigger(trigger);
		HashMap<String, Integer> result = new HashMap<>();
		result.put("triggered_rules", eventcount);
		return result;
	}

	@Override
	@WebRequest(path = "rules", description = "List all event-rules of the controlcenter. A rule can be triggered by the speicified trigger.")
	public List<EventRule> getEvents(@WebGet(name = "token") String token) throws RemoteException {
		UserSessionHandler.require(token);
		return mCenter.getEventRules();
	}

	@Override
	@WebRequest(path = "create_event_rule", description = "Create new event rule for given trigger id.")
	public EventRule createEventRule(@WebGet(name = "token") String token, @WebGet(name = "trigger") String triggerID)
			throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		EventRule rule = new EventRule();
		rule.setTriggerID(triggerID);
		rule.setControlcenter(mCenter);
		mCenter.addEventRule(rule);
		return rule;
	}

	@Override
	@WebRequest(path = "delete_event_rule", description = "Delete event rule by given trigger id.")
	public void deleteEventRule(@WebGet(name = "token") String token, @WebGet(name = "trigger") String triggerID)
			throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		mCenter.deleteEventRule(triggerID);
	}

	@Override
	@WebRequest(path = "create_event_for_rule", description = "Create new event for given event rule. The event corresponds to a specifig unit and can have an optional condition.")
	public EventRule createEventForRule(@WebGet(name = "token") String token,
			@WebGet(name = "trigger") String triggerID, @WebGet(name = "unit") String unitID,
			@WebGet(name = "condition") String condition) throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		EventRule rule = mCenter.getEventRule(triggerID);
		if (rule == null)
			throw new IllegalArgumentException("Event rule with trigger id '" + triggerID + "' does not exists!");
		Event event = new Event();
		event.setUnitID(unitID);
		event.setCondition(condition);
		rule.getEvents().add(event);

		Dao<Event> eventDao = DaoFactory.getInstance().getDao(Event.class);
		eventDao.save(event);

		Dao<EventRule> eventRuleDao = DaoFactory.getInstance().getDao(EventRule.class);
		eventRuleDao.update(rule);
		return rule;
	}

	@Override
	@WebRequest(path = "delete_event_in_rule", description = "Delete event in given event rule by event index.")
	public void deleteEventInRule(@WebGet(name = "token") String token, @WebGet(name = "trigger") String triggerID,
			@WebGet(name = "index") int index) throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		EventRule rule = mCenter.getEventRule(triggerID);
		if (rule == null)
			throw new IllegalArgumentException("Event rule with trigger id '" + triggerID + "' does not exists!");
		if (rule.getEvents().size() <= index)
			throw new IllegalArgumentException(
					"Event index out of range. Event rule has " + rule.getEvents().size() + " event(s).");
		Event event = rule.getEvents().get(index);
		rule.getEvents().remove(index);

		Dao<Event> eventDao = DaoFactory.getInstance().getDao(Event.class);
		eventDao.delete(event);

		Dao<EventRule> eventRuleDao = DaoFactory.getInstance().getDao(EventRule.class);
		eventRuleDao.update(rule);
	}

	@Override
	@WebRequest(path = "add_parameter_for_event", description = "Add parameter for event given event rule by event index.")
	public EventRule addParameterforEventInRule(@WebGet(name = "token") String token,
			@WebGet(name = "trigger") String triggerID, @WebGet(name = "index") int index,
			@WebGet(name = "key") String key, @WebGet(name = "value") String value)
			throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		EventRule rule = mCenter.getEventRule(triggerID);
		if (rule == null)
			throw new IllegalArgumentException("Event rule with trigger id '" + triggerID + "' does not exists!");
		if (rule.getEvents().size() <= index)
			throw new IllegalArgumentException(
					"Event index out of range. Event rule has " + rule.getEvents().size() + " event(s).");
		Event event = rule.getEvents().get(index);
		Parameter param = new Parameter();
		param.mKey = key;
		param.mValue = value;
		event.addParameter(param);

		Dao<Parameter> paramDao = DaoFactory.getInstance().getDao(Parameter.class);
		paramDao.save(param);

		Dao<Event> eventDao = DaoFactory.getInstance().getDao(Event.class);
		eventDao.update(event);
		return rule;
	}

	@Override
	@WebRequest(path = "delete_parameter_for_event", description = "Delete parameter for event given event rule by event index and parameter index.")
	public EventRule deleteParameterforEventInRule(@WebGet(name = "token") String token,
			@WebGet(name = "trigger") String triggerID, @WebGet(name = "event_index") int eventIndex,
			@WebGet(name = "parameter_index") int parameterIndex) throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		EventRule rule = mCenter.getEventRule(triggerID);
		if (rule == null)
			throw new IllegalArgumentException("Event rule with trigger id '" + triggerID + "' does not exists!");
		if (rule.getEvents().size() <= eventIndex)
			throw new IllegalArgumentException(
					"Event index out of range. Event rule has " + rule.getEvents().size() + " event(s).");
		Event event = rule.getEvents().get(eventIndex);
		if (event.getParameter().size() <= parameterIndex)
			throw new IllegalArgumentException(
					"Parameter index out of range. Event has " + event.getParameter().size() + " parameter(s).");
		Parameter param = event.getParameteList().get(parameterIndex);
		event.deleteParameter(param);

		Dao<Parameter> paramDao = DaoFactory.getInstance().getDao(Parameter.class);
		paramDao.delete(param);

		Dao<Event> eventDao = DaoFactory.getInstance().getDao(Event.class);
		eventDao.update(event);
		return rule;
	}

	@Override
	@WebRequest(path = "set_information_for_event_rule", description = "Set informations for event given event rule. Several information are separated by comma.")
	public EventRule setInformationsforEventRule(@WebGet(name = "token") String token,
			@WebGet(name = "trigger") String triggerID, @WebGet(name = "informations") String informations)
			throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		EventRule rule = mCenter.getEventRule(triggerID);
		if (rule == null)
			throw new IllegalArgumentException("Event rule with trigger id '" + triggerID + "' does not exists!");
		rule.setInformation(informations);

		Dao<EventRule> eventRuleDao = DaoFactory.getInstance().getDao(EventRule.class);
		eventRuleDao.update(rule);

		return rule;
	}

	@Override
	@WebRequest(path = "set_condition_for_event_in_rule", description = "Set condition for an event of an given event rule.")
	public EventRule setConditionforEvent(@WebGet(name = "token") String token,
			@WebGet(name = "trigger") String triggerID, @WebGet(name = "event_index") int eventIndex,
			@WebGet(name = "condition") String condition) throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		EventRule rule = mCenter.getEventRule(triggerID);
		if (rule == null)
			throw new IllegalArgumentException("Event rule with trigger id '" + triggerID + "' does not exists!");
		if (rule.getEvents().size() <= eventIndex)
			throw new IllegalArgumentException(
					"Event index out of range. Event rule has " + rule.getEvents().size() + " event(s).");
		Event event = rule.getEvents().get(eventIndex);
		event.setCondition(condition);

		Dao<Event> eventDao = DaoFactory.getInstance().getDao(Event.class);
		eventDao.update(event);
		return rule;
	}

	private TimeTriggerBean toTTBean(CronJobTrigger trigger) {
		TimeTriggerBean triggerBean = new TimeTriggerBean();
		triggerBean.CroneJob = trigger.getCronDescription();
		triggerBean.Id = trigger.getId();
		if (trigger.getTriggerList().size() > 0) {
			triggerBean.TriggerId = trigger.getTriggerList().get(0).getTriggerID();
		}
		return triggerBean;
	}

	@Override
	@WebRequest(path = "list_timetrigger", description = "List all crone-job time trigger.", genericClass = TimeTriggerBean.class)
	public ArrayList<TimeTriggerBean> getTimeTrigger(@WebGet(name = "token") String token)
			throws RemoteException, DaoException {
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
		CronJobTrigger trigger = new CronJobTrigger();
		trigger.setControlCenter(mCenter);
		trigger.setCronDescription(cronJob);
		Trigger t = new Trigger();
		t.setTriggerID(triggerId);
		trigger.getTriggerList().add(t);
		mCenter.addCronTrigger(trigger);
		return toTTBean(trigger);
	}

	@Override
	@WebRequest(path = "delete_timetrigger", description = "Delete crone-job time trigger for trigger id.")
	public void deleteTimeTrigger(@WebGet(name = "token") String token, @WebGet(name = "id") long id)
			throws RemoteException, DaoException {
		CronJobTrigger trigger = mCenter.getCronTrigger(id);
		if (trigger == null) {
			throw new RemoteException("Unknown timetrigger id " + id);
		}
		mCenter.deleteCronTrigger(trigger);
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
