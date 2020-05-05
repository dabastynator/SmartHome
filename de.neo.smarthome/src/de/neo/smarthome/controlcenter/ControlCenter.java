package de.neo.smarthome.controlcenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.neo.persist.Dao;
import de.neo.persist.DaoException;
import de.neo.persist.DaoFactory;
import de.neo.persist.annotations.Domain;
import de.neo.persist.annotations.OnLoad;
import de.neo.persist.annotations.OneToMany;
import de.neo.persist.annotations.Persist;
import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebGet;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.api.Event;
import de.neo.smarthome.api.EventRule;
import de.neo.smarthome.api.GroundPlot;
import de.neo.smarthome.api.IControlCenter;
import de.neo.smarthome.api.IControllUnit;
import de.neo.smarthome.api.Trigger;
import de.neo.smarthome.api.Trigger.Parameter;
import de.neo.smarthome.informations.WebInformation;
import de.neo.smarthome.user.UnitAccessHandler;

/**
 * Implement the control center interface.
 * 
 * @author sebastian
 */
@Domain
public class ControlCenter implements IControlCenter {

	@OneToMany(domainClass = EventRule.class, name = "EventRule")
	private List<EventRule> mEventRules = Collections.synchronizedList(new ArrayList<EventRule>());

	private Map<String, EventRule> mEventRuleMap = new HashMap<>();

	@OneToMany(domainClass = CronJobTrigger.class, name = "TimeTrigger")
	private List<CronJobTrigger> mCronjobTrigger = Collections.synchronizedList(new ArrayList<CronJobTrigger>());

	@OneToMany(domainClass = Trigger.class, name = "StartTrigger")
	private List<Trigger> mStartUpTrigger = new ArrayList<>();

	@Persist(name = "GroundPlot")
	private GroundPlot mGround = new GroundPlot();

	@Persist(name = "port")
	private int mPort;

	/**
	 * List of all control units
	 */
	private Map<String, IControllUnit> mControlUnits = Collections
			.synchronizedMap(new HashMap<String, IControllUnit>());

	private EventWorker mEventWorker = new EventWorker(this);

	private WebInformation mInformation;

	private UnitAccessHandler mAccessHandler = new UnitAccessHandler(this);

	@OnLoad
	public void onLoad() {
		mEventRuleMap.clear();
		for (EventRule rule : mEventRules) {
			rule.setControlcenter(this);
			mEventRuleMap.put(rule.getTriggerID(), rule);
		}
		for (CronJobTrigger trigger : mCronjobTrigger)
			trigger.setControlCenter(this);
	}

	@Override
	public void addControlUnit(IControllUnit controlUnit) {
		try {
			controlUnit.setControlCenter(this);
			String id = controlUnit.getID();
			mControlUnits.put(id, controlUnit);
			RemoteLogger.performLog(LogPriority.INFORMATION,
					"Add " + mControlUnits.size() + ". control unit: " + controlUnit.getName() + " (" + id + ")",
					"Controlcenter");
		} catch (RemoteException e) {
			RemoteLogger.performLog(LogPriority.ERROR, "Could not add control unit: " + e.getMessage(), "");
		}
	}

	@Override
	public void removeControlUnit(IControllUnit controlUnit) {
		try {
			mControlUnits.remove(controlUnit.getID());
		} catch (RemoteException e) {
			RemoteLogger.performLog(LogPriority.ERROR, "Could not remove control unit: " + e.getMessage(), "");

		}
	}

	@Override
	@WebRequest(path = "groundplot", description = "Get the ground-plot for this control center.")
	public GroundPlot getGroundPlot() {
		return mGround;
	}

	@Override
	public IControllUnit getControlUnit(String id) {
		return mControlUnits.get(id);
	}

	@Override
	public int trigger(Trigger trigger) {
		int eventCount = 0;
		EventRule rule = mEventRuleMap.get(trigger.getTriggerID());
		if (rule != null) {
			try {
				for (Event event : rule.getEventsForTrigger(trigger)) {
					event.getParameter().putAll(trigger.getParameter());
					mEventWorker.queueEvent(event);
					eventCount++;
				}
			} catch (Exception e) {
				RemoteLogger.performLog(LogPriority.ERROR,
						"Perform event: " + e.getClass().getSimpleName() + ": " + e.getMessage(), "controlcenter");
			}
		}
		RemoteLogger.performLog(LogPriority.INFORMATION, "Perform trigger with " + eventCount + " event(s)",
				trigger.getTriggerID());
		return eventCount;
	}

	@WebRequest(description = "Perform specified trigger", path = "dotrigger", genericClass = Integer.class)
	public HashMap<String, Integer> performTrigger(@WebGet(name = "trigger") String triggerID) {
		Trigger trigger = new Trigger();
		trigger.setTriggerID(triggerID);
		int eventcount = trigger(trigger);
		HashMap<String, Integer> result = new HashMap<>();
		result.put("triggered_rules", eventcount);
		return result;
	}

	@WebRequest(path = "rules", description = "List all event-rules of the controlcenter. A rule can be triggered by the speicified trigger.")
	public List<EventRule> getEvents() {
		return mEventRules;
	}

	@WebRequest(path = "create_event_rule", description = "Create new event rule for given trigger id.")
	public EventRule createEventRule(@WebGet(name = "trigger") String triggerID) throws RemoteException, DaoException {
		if (mEventRuleMap.containsKey(triggerID))
			throw new IllegalArgumentException("Event rule with trigger id '" + triggerID + "' already exists!");
		EventRule rule = new EventRule();
		rule.setTriggerID(triggerID);
		rule.setControlcenter(this);
		mEventRules.add(rule);
		mEventRuleMap.put(triggerID, rule);

		Dao<EventRule> eventRuleDao = DaoFactory.getInstance().getDao(EventRule.class);
		eventRuleDao.save(rule);

		Dao<ControlCenter> centerDao = DaoFactory.getInstance().getDao(ControlCenter.class);
		centerDao.update(this);
		return rule;
	}

	@WebRequest(path = "delete_event_rule", description = "Delete event rule by given trigger id.")
	public void deleteEventRule(@WebGet(name = "trigger") String triggerID) throws RemoteException, DaoException {
		if (!mEventRuleMap.containsKey(triggerID))
			throw new IllegalArgumentException("Event rule with trigger id '" + triggerID + "' does not exists!");
		EventRule rule = mEventRuleMap.get(triggerID);
		mEventRules.remove(rule);
		mEventRuleMap.remove(triggerID);

		Dao<EventRule> eventRuleDao = DaoFactory.getInstance().getDao(EventRule.class);
		eventRuleDao.delete(rule);

		Dao<ControlCenter> centerDao = DaoFactory.getInstance().getDao(ControlCenter.class);
		centerDao.update(this);
	}

	@WebRequest(path = "create_event_for_rule", description = "Create new event for given event rule. The event corresponds to a specifig unit and can have an optional condition.")
	public EventRule createEventForRule(@WebGet(name = "trigger") String triggerID,
			@WebGet(name = "unit") String unitID, @WebGet(name = "condition") String condition)
			throws RemoteException, DaoException {
		if (!mEventRuleMap.containsKey(triggerID))
			throw new IllegalArgumentException("Event rule with trigger id '" + triggerID + "' does not exists!");
		EventRule rule = mEventRuleMap.get(triggerID);
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

	@WebRequest(path = "delete_event_in_rule", description = "Delete event in given event rule by event index.")
	public void deleteEventInRule(@WebGet(name = "trigger") String triggerID, @WebGet(name = "index") int index)
			throws RemoteException, DaoException {
		if (!mEventRuleMap.containsKey(triggerID))
			throw new IllegalArgumentException("Event rule with trigger id '" + triggerID + "' does not exists!");
		EventRule rule = mEventRuleMap.get(triggerID);
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

	@WebRequest(path = "add_parameter_for_event", description = "Add parameter for event given event rule by event index.")
	public EventRule addParameterforEventInRule(@WebGet(name = "trigger") String triggerID,
			@WebGet(name = "index") int index, @WebGet(name = "key") String key, @WebGet(name = "value") String value)
			throws RemoteException, DaoException {
		if (!mEventRuleMap.containsKey(triggerID))
			throw new IllegalArgumentException("Event rule with trigger id '" + triggerID + "' does not exists!");
		EventRule rule = mEventRuleMap.get(triggerID);
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

	@WebRequest(path = "delete_parameter_for_event", description = "Delete parameter for event given event rule by event index and parameter index.")
	public EventRule deleteParameterforEventInRule(@WebGet(name = "trigger") String triggerID,
			@WebGet(name = "event_index") int eventIndex, @WebGet(name = "parameter_index") int parameterIndex)
			throws RemoteException, DaoException {
		if (!mEventRuleMap.containsKey(triggerID))
			throw new IllegalArgumentException("Event rule with trigger id '" + triggerID + "' does not exists!");
		EventRule rule = mEventRuleMap.get(triggerID);
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
	public EventRule setInformationsforEventRule(@WebGet(name = "trigger") String triggerID,
			@WebGet(name = "informations") String informations) throws RemoteException, DaoException {
		if (!mEventRuleMap.containsKey(triggerID))
			throw new IllegalArgumentException("Event rule with trigger id '" + triggerID + "' does not exists!");
		EventRule rule = mEventRuleMap.get(triggerID);
		rule.setInformation(informations);

		Dao<EventRule> eventRuleDao = DaoFactory.getInstance().getDao(EventRule.class);
		eventRuleDao.update(rule);

		return rule;
	}

	@WebRequest(path = "set_condition_for_event_in_rule", description = "Set condition for an event of an given event rule.")
	public EventRule setConditionforEvent(@WebGet(name = "trigger") String triggerID,
			@WebGet(name = "event_index") int eventIndex, @WebGet(name = "condition") String condition)
			throws RemoteException, DaoException {
		if (!mEventRuleMap.containsKey(triggerID))
			throw new IllegalArgumentException("Event rule with trigger id '" + triggerID + "' does not exists!");
		EventRule rule = mEventRuleMap.get(triggerID);
		if (rule.getEvents().size() <= eventIndex)
			throw new IllegalArgumentException(
					"Event index out of range. Event rule has " + rule.getEvents().size() + " event(s).");
		Event event = rule.getEvents().get(eventIndex);
		event.setCondition(condition);

		Dao<Event> eventDao = DaoFactory.getInstance().getDao(Event.class);
		eventDao.update(event);
		return rule;
	}

	@Override
	public Map<String, IControllUnit> getControlUnits() {
		return mControlUnits;
	}

	public void setInformationHandler(WebInformation info) {
		mInformation = info;
	}

	public WebInformation getInformationHandler() {
		return mInformation;
	}

	public UnitAccessHandler getAccessHandler() {
		return mAccessHandler;
	}

	public int getPort() {
		return mPort;
	}

	public void setPort(int port) {
		mPort = port;
	}

	public List<Trigger> getStartupTrigger() {
		return mStartUpTrigger;
	}

	public void schedule() {
		for (CronJobTrigger job : mCronjobTrigger)
			job.schedule();
	}
}
