package de.neo.remote.controlcenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.neo.remote.RemoteLogger;
import de.neo.remote.api.Event;
import de.neo.remote.api.GroundPlot;
import de.neo.remote.api.GroundPlot.Feature;
import de.neo.remote.api.GroundPlot.Point;
import de.neo.remote.api.GroundPlot.Wall;
import de.neo.remote.api.IControlCenter;
import de.neo.remote.api.IControlUnit;
import de.neo.remote.api.Trigger;
import de.neo.rmi.api.RMILogger.LogPriority;
import de.neo.rmi.api.WebField;
import de.neo.rmi.api.WebGet;
import de.neo.rmi.api.WebRequest;
import de.neo.rmi.protokol.RemoteException;

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
	private Map<String, IControlUnit> mControlUnits = Collections.synchronizedMap(new HashMap<String, IControlUnit>());

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

	public ControlCenterImpl(Node root) {
		mGround = readGround((Element) root);
		mEventWorker = new EventWorker(this);
		mStartUpTrigger = new ArrayList<String>();
		start();
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
				Thread.sleep(100 * 10);
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * remove control units with exception
	 */
	private void checkControlUnits() {
		int removed = 0;
		for (String id : mControlUnits.keySet()) {
			try {
				IControlUnit unit = mControlUnits.get(id);
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
	public void addControlUnit(IControlUnit controlUnit) {
		try {
			String id = controlUnit.getID();
			mControlUnits.put(id, controlUnit);
			RemoteLogger.performLog(LogPriority.INFORMATION,
					"Add " + mControlUnits.size() + ". control unit: " + controlUnit.getName() + " (" + id + ")",
					"Controlcenter");
		} catch (RemoteException e) {

		}
	}

	@Override
	public void removeControlUnit(IControlUnit controlUnit) {
		mControlUnits.remove(controlUnit);
	}

	@Override
	public GroundPlot getGroundPlot() {
		return mGround;
	}

	@Override
	public String[] getControlUnitIDs() {
		return mControlUnits.keySet().toArray(new String[mControlUnits.size()]);
	}

	@Override
	public IControlUnit getControlUnit(String id) {
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
			EventRule rule = new EventRule();
			rule.initialize(element);
			mEventRules.add(rule);

		}
	}

	public static class EventRule implements IEventRule {

		@WebField(name = "events")
		private List<Event> mEvents = new ArrayList<Event>();

		@WebField(name = "condition")
		private String mCondition;

		@WebField(name = "trigger")
		private String mTrigger;

		public void initialize(Element xmlElement) throws SAXException {
			for (String attribute : new String[] { "trigger" })
				if (!xmlElement.hasAttribute(attribute))
					throw new SAXException(attribute + " missing for " + getClass().getSimpleName());
			mTrigger = xmlElement.getAttribute("trigger");
			if (xmlElement.hasAttribute("condition"))
				mCondition = xmlElement.getAttribute("condition");
			NodeList childNodes = xmlElement.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				if (childNodes.item(i) instanceof Element) {
					Element element = (Element) childNodes.item(i);
					Event event = new Event();
					event.initialize(element);
					mEvents.add(event);
				}
			}
		}

		@Override
		public Event[] getEventsForTrigger(Trigger trigger) throws RemoteException {
			boolean fireEvents = trigger.getTriggerID().equals(mTrigger);
			if (mCondition != null) {
				fireEvents = mCondition.equals(trigger.getParameter("condition"));
			}
			if (fireEvents)
				return mEvents.toArray(new Event[mEvents.size()]);
			return null;
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
	public Map<String, Integer> performTrigger(@WebGet(name = "trigger") String triggerID) {
		Trigger trigger = new Trigger();
		trigger.setTriggerID(triggerID);
		int eventcount = trigger(trigger);
		Map<String, Integer> result = new HashMap<>();
		result.put("triggered_rules", eventcount);
		return result;
	}

	@WebRequest(path = "rules", description = "List all event-rules of the controlcenter. A rule can be triggered by the speicified trigger.")
	public List<IEventRule> getEvents() {
		return mEventRules;
	}

	@Override
	public Map<String, IControlUnit> getControlUnits() {
		return mControlUnits;
	}

}
