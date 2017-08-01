package de.neo.smarthome.controlcenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebGet;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.api.Event;
import de.neo.smarthome.api.GroundPlot;
import de.neo.smarthome.api.GroundPlot.Feature;
import de.neo.smarthome.api.GroundPlot.Point;
import de.neo.smarthome.api.GroundPlot.Wall;
import de.neo.smarthome.api.Trigger;
import de.neo.smarthome.informations.WebInformation;

/**
 * Implement the control center interface.
 * 
 * @author sebastian
 */
public class ControlCenterImpl extends Thread implements IControlCenter {

	public static String ROOT = "GroundPlot";

	/**
	 * List of all control units
	 */
	private Map<String, IControllUnit> mControlUnits = Collections
			.synchronizedMap(new HashMap<String, IControllUnit>());

	/**
	 * List of all event rules
	 */
	private List<IEventRule> mEventRules = Collections.synchronizedList(new ArrayList<IEventRule>());

	/**
	 * List of all cronjob trigger
	 */
	private List<CronJobTrigger> mCronjobTrigger = Collections.synchronizedList(new ArrayList<CronJobTrigger>());

	/**
	 * The ground plot of the control center area
	 */
	private GroundPlot mGround;

	private EventWorker mEventWorker;

	private List<String> mStartUpTrigger;

	private WebInformation mInformation;

	public ControlCenterImpl() {
		mGround = new GroundPlot();
		mEventWorker = new EventWorker(this);
		mStartUpTrigger = new ArrayList<String>();
		start();
	}

	public void initializeGroundPlot(Node root) {
		mGround = readGround((Element) root);
	}

	private GroundPlot readGround(Element root) {
		GroundPlot ground = new GroundPlot();
		NodeList walls = root.getElementsByTagName("Wall");
		for (int i = 0; i < walls.getLength(); i++) {
			Element wallElement = (Element) walls.item(i);
			Wall wall = new GroundPlot.Wall();
			NodeList points = wallElement.getElementsByTagName("Point");
			for (int j = 0; j < points.getLength(); j++) {
				Point point = new GroundPlot.Point();
				Element pointElement = (Element) points.item(j);
				point.x = Float.parseFloat(pointElement.getAttribute("x"));
				point.y = Float.parseFloat(pointElement.getAttribute("y"));
				point.z = Float.parseFloat(pointElement.getAttribute("z"));
				wall.points.add(point);
			}
			ground.walls.add(wall);
		}
		NodeList features = root.getElementsByTagName("Feature");
		for (int i = 0; i < features.getLength(); i++) {

			// read basics
			Element featureElement = (Element) features.item(i);
			Feature feature = new Feature();
			feature.x = Float.parseFloat(featureElement.getAttribute("x"));
			feature.y = Float.parseFloat(featureElement.getAttribute("y"));
			feature.z = Float.parseFloat(featureElement.getAttribute("z"));
			feature.az = Float.parseFloat(featureElement.getAttribute("az"));
			feature.type = featureElement.getAttribute("type");
			if (featureElement.hasAttribute("extra"))
				feature.extra = featureElement.getAttribute("extra");

			ground.features.add(feature);
		}
		return ground;
	}

	@Override
	public void run() {
		while (true) {
			checkControlUnits();
			try {
				Thread.sleep(1000 * 30);
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * remove control units with exception
	 */
	private void checkControlUnits() {
		int removed = 0;
		for (String id : new HashSet<>(mControlUnits.keySet())) {
			try {
				IControllUnit unit = mControlUnits.get(id);
				unit.getID();
			} catch (RemoteException e) {
				mControlUnits.remove(id);
				removed++;
			}
		}
		if (removed > 0)
			RemoteLogger.performLog(LogPriority.WARNING,
					"Lost " + removed + " unit(s). " + mControlUnits.size() + " unit(s) left.", "Controlcenter");
	}

	@Override
	public void addControlUnit(IControllUnit controlUnit) {
		try {
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
	public String[] getControlUnitIDs() {
		return mControlUnits.keySet().toArray(new String[mControlUnits.size()]);
	}

	@Override
	public IControllUnit getControlUnit(String id) {
		return mControlUnits.get(id);
	}

	@Override
	public int trigger(Trigger trigger) {
		int eventCount = 0;
		for (IEventRule rule : mEventRules) {
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

	public void initializeRules(NodeList nodes) throws SAXException {
		mEventRules.clear();
		for (int i = 0; i < nodes.getLength(); i++) {
			Element element = (Element) nodes.item(i);
			EventRule rule = new EventRule(this);
			rule.initialize(element);
			mEventRules.add(rule);

		}
	}

	public void initializeTrigger(NodeList nodeList) throws SAXException {
		for (int i = 0; i < nodeList.getLength(); i++)
			if (nodeList.item(i) instanceof Element) {
				Element element = (Element) nodeList.item(i);
				CronJobTrigger trigger = new CronJobTrigger(this);
				trigger.initialize(element);
				trigger.schedule();
				mCronjobTrigger.add(trigger);
			}
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
	public List<IEventRule> getEvents() {
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

}
