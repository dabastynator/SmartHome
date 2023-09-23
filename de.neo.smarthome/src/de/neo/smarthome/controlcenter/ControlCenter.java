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
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.api.Event;
import de.neo.smarthome.api.IControlCenter;
import de.neo.smarthome.api.IControllUnit;
import de.neo.smarthome.api.Script;
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

	@OneToMany(domainClass = Script.class, name = "Script")
	private List<Script> mScripts = Collections.synchronizedList(new ArrayList<Script>());

	private Map<String, Script> mScriptMap = new HashMap<>();

	@OneToMany(domainClass = CronJobTrigger.class, name = "TimeTrigger")
	private List<CronJobTrigger> mCronjobTrigger = Collections.synchronizedList(new ArrayList<CronJobTrigger>());

	@OneToMany(domainClass = Trigger.class, name = "StartTrigger")
	private List<Trigger> mStartUpTrigger = new ArrayList<>();

	@Persist(name = "port")
	private int mPort;
	
	@Persist(name = "hassToken")
	private String mHassToken;
	
	@Persist(name = "hassUrl")
	private String mHassUrl;

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
		mScriptMap.clear();
		for (Script script : mScripts) {
			script.setControlcenter(this);
			mScriptMap.put(script.getTriggerID(), script);
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
	public IControllUnit getControlUnit(String id) {
		return mControlUnits.get(id);
	}

	@Override
	public int trigger(Trigger trigger) {
		int eventCount = 0;
		Script script = mScriptMap.get(trigger.getTriggerID());
		if (script != null) {
			try {
				for (Event event : script.getEventsForTrigger(trigger)) {
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
	
	public String getHassToken() {
		return mHassToken;
	}
	
	public String getHassUrl() {
		return mHassUrl;
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
	public List<Script> getScripts() {
		return mScripts;
	}

	@Override
	public Script getScript(String id) {
		return mScriptMap.get(id);
	}

	@Override
	public void addScript(Script script) throws DaoException {
		if (mScriptMap.containsKey(script.getTriggerID()))
			throw new IllegalArgumentException(
					"Script with trigger id '" + script.getTriggerID() + "' already exists!");

		mScripts.add(script);
		mScriptMap.put(script.getTriggerID(), script);

		Dao<Script> scriptDao = DaoFactory.getInstance().getDao(Script.class);
		scriptDao.save(script);

		Dao<ControlCenter> centerDao = DaoFactory.getInstance().getDao(ControlCenter.class);
		centerDao.update(this);
	}

	@Override
	public void deleteScript(String triggerID) throws DaoException {
		if (!mScriptMap.containsKey(triggerID))
			throw new IllegalArgumentException("Script with trigger id '" + triggerID + "' does not exists!");
		Script script = mScriptMap.get(triggerID);
		mScripts.remove(script);
		mScriptMap.remove(triggerID);

		Dao<Event> eventDao = DaoFactory.getInstance().getDao(Event.class);
		for (Event event: script.getEvents()) {
			eventDao.delete(event);
		}
		
		Dao<Script> scriptDao = DaoFactory.getInstance().getDao(Script.class);
		scriptDao.delete(script);

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
		trigger.schedule();

		Dao<CronJobTrigger> scriptDao = DaoFactory.getInstance().getDao(CronJobTrigger.class);
		scriptDao.save(trigger);

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

		Dao<CronJobTrigger> scriptDao = DaoFactory.getInstance().getDao(CronJobTrigger.class);
		scriptDao.delete(trigger);

		Dao<ControlCenter> centerDao = DaoFactory.getInstance().getDao(ControlCenter.class);
		centerDao.update(this);
	}

}
