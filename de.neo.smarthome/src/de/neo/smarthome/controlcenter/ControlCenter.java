package de.neo.smarthome.controlcenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.neo.persist.annotations.Domain;
import de.neo.persist.annotations.OneToMany;
import de.neo.persist.annotations.Persist;
import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebGet;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.api.Event;
import de.neo.smarthome.api.GroundPlot;
import de.neo.smarthome.api.Trigger;
import de.neo.smarthome.informations.WebInformation;

/**
 * Implement the control center interface.
 * 
 * @author sebastian
 */
@Domain
public class ControlCenter implements IControlCenter {

	@OneToMany(domainClass = EventRule.class, name = "EventRule")
	private List<EventRule> mEventRules = Collections.synchronizedList(new ArrayList<EventRule>());

	@OneToMany(domainClass = CronJobTrigger.class, name = "TimeTrigger")
	private List<CronJobTrigger> mCronjobTrigger = Collections.synchronizedList(new ArrayList<CronJobTrigger>());

	@OneToMany(domainClass = Trigger.class, name = "StartTrigger")
	private List<Trigger> mStartUpTrigger = new ArrayList<>();

	@Persist(name = "GroundPlot")
	private GroundPlot mGround = new GroundPlot();

	@Persist(name = "port")
	private int mPort;

	@Persist(name = "token")
	private String mToken;

	/**
	 * List of all control units
	 */
	private Map<String, IControllUnit> mControlUnits = Collections
			.synchronizedMap(new HashMap<String, IControllUnit>());

	private EventWorker mEventWorker = new EventWorker(this);

	private WebInformation mInformation;

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
		mControlUnits.remove(controlUnit);
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
		for (EventRule rule : mEventRules) {
			try {
				Event[] events = rule.getEventsForTrigger(trigger);
				if (events != null) {
					for (Event event : events) {
						event.getParameter().putAll(trigger.getParameter());
						mEventWorker.queueEvent(event);
					}
					eventCount += events.length;
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

	public int getPort() {
		return mPort;
	}

	public void setPort(int port) {
		mPort = port;
	}

	public String getToken() {
		return mToken;
	}

	public void setToken(String token) {
		mToken = token;
	}

	public List<Trigger> getStartupTrigger() {
		return mStartUpTrigger;
	}

	public void schedule() {
		for (CronJobTrigger job : mCronjobTrigger)
			job.schedule();
	}

}
