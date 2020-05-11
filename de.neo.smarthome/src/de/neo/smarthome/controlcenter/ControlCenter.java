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
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.api.Event;
import de.neo.smarthome.api.EventRule;
import de.neo.smarthome.api.GroundPlot;
import de.neo.smarthome.api.IControlCenter;
import de.neo.smarthome.api.IControllUnit;
import de.neo.smarthome.api.Trigger;
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

	public void onPostLoad() {
		mAccessHandler.initialize();
	}

	@Override
	public List<EventRule> getEventRules() {
		return mEventRules;
	}

	@Override
	public EventRule getEventRule(String id) {
		return mEventRuleMap.get(id);
	}

	@Override
	public void addEventRule(EventRule rule) throws DaoException {
		if (mEventRuleMap.containsKey(rule.getTriggerID()))
			throw new IllegalArgumentException(
					"Event rule with trigger id '" + rule.getTriggerID() + "' already exists!");

		mEventRules.add(rule);
		mEventRuleMap.put(rule.getTriggerID(), rule);

		Dao<EventRule> eventRuleDao = DaoFactory.getInstance().getDao(EventRule.class);
		eventRuleDao.save(rule);

		Dao<ControlCenter> centerDao = DaoFactory.getInstance().getDao(ControlCenter.class);
		centerDao.update(this);
	}

	@Override
	public void deleteEventRule(String triggerID) throws DaoException {
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

	@Override
	public List<CronJobTrigger> getCronTriggers() {
		return mCronjobTrigger;
	}

	@Override
	public void addCronTrigger(CronJobTrigger trigger) throws DaoException {
		mCronjobTrigger.add(trigger);

		Dao<CronJobTrigger> eventRuleDao = DaoFactory.getInstance().getDao(CronJobTrigger.class);
		eventRuleDao.save(trigger);

		Dao<ControlCenter> centerDao = DaoFactory.getInstance().getDao(ControlCenter.class);
		centerDao.update(this);
	}

	@Override
	public CronJobTrigger getCronTrigger(long id) throws DaoException {
		Dao<CronJobTrigger> dao = DaoFactory.getInstance().getDao(CronJobTrigger.class);
		return dao.loadById(id);
	}

	@Override
	public void deleteCronTrigger(CronJobTrigger trigger) throws DaoException {
		mCronjobTrigger.remove(trigger);

		Dao<CronJobTrigger> eventRuleDao = DaoFactory.getInstance().getDao(CronJobTrigger.class);
		eventRuleDao.delete(trigger);

		Dao<ControlCenter> centerDao = DaoFactory.getInstance().getDao(ControlCenter.class);
		centerDao.update(this);
	}

}
