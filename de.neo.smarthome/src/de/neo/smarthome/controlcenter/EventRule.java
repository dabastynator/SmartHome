package de.neo.smarthome.controlcenter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebField;
import de.neo.smarthome.api.Event;
import de.neo.smarthome.api.IWebInformationUnit.InformationEntryBean;
import de.neo.smarthome.controlcenter.IControlCenter.IEventRule;
import de.neo.smarthome.api.Trigger;

public class EventRule implements IEventRule {

	@WebField(name = "events")
	private List<Event> mEvents = new ArrayList<Event>();

	@WebField(name = "trigger")
	private String mTrigger;

	@WebField(name = "information")
	private List<String> mInformation = new ArrayList<>();

	private ControlCenterImpl mCenter;

	public EventRule(ControlCenterImpl center) {
		mCenter = center;
	}

	public void initialize(Element xmlElement) throws SAXException {
		for (String attribute : new String[] { "trigger" })
			if (!xmlElement.hasAttribute(attribute))
				throw new SAXException(attribute + " missing for " + getClass().getSimpleName());
		mTrigger = xmlElement.getAttribute("trigger");
		NodeList events = xmlElement.getElementsByTagName("Event");
		for (int i = 0; i < events.getLength(); i++) {
			if (events.item(i) instanceof Element) {
				Element element = (Element) events.item(i);
				Event event = new Event();
				event.initialize(element);
				mEvents.add(event);
			}
		}
		NodeList infos = xmlElement.getElementsByTagName("Information");
		for (int i = 0; i < infos.getLength(); i++) {
			if (infos.item(i) instanceof Element) {
				Element element = (Element) infos.item(i);
				if (element.hasAttribute("key"))
					mInformation.add(element.getAttribute("key"));
				else
					throw new SAXException("Informaiton-tag needs key attribute!");
			}
		}
	}

	@Override
	public Event[] getEventsForTrigger(Trigger trigger) throws RemoteException {
		boolean fireEvents = trigger.getTriggerID().equals(mTrigger);
		if (fireEvents) {
			Map<String, String> parameters = new HashMap<>();
			parameters.putAll(trigger.getParameter());
			for (String key : mInformation) {
				fillMapByInformation(parameters, key);
			}
			List<Event> events = new ArrayList<>();
			for (Event e : mEvents) {
				if (evaluateCondition(parameters, e.getCondition()))
					events.add(e);
			}
			return events.toArray(new Event[events.size()]);
		}
		return null;
	}

	private boolean evaluateCondition(Map<String, String> parameters, String condition) {
		if (condition == null)
			return true;
		if (condition.contains(" and ")) {
			for (String c : condition.split(" and "))
				if (!evaluateCondition(parameters, c))
					return false;
			return true;
		}
		if (condition.contains(" or ")) {
			for (String c : condition.split(" or "))
				if (evaluateCondition(parameters, c))
					return true;
			return false;
		}
		if (condition.contains("=")) {
			String[] c = condition.split("=");
			prepareCondition(c, parameters);
			if (c.length > 1)
				return c[0].equals(c[1]);
		}
		if (condition.contains("<")) {
			String[] c = condition.split("<");
			prepareCondition(c, parameters);
			try {
				if (c.length > 1)
					return Double.valueOf(c[0]) < Double.valueOf(c[1]);
			} catch (Exception e) {
				// ignore
			}
		}
		if (condition.contains(">")) {
			String[] c = condition.split(">");
			prepareCondition(c, parameters);
			try {
				if (c.length > 1)
					return Double.valueOf(c[0]) > Double.valueOf(c[1]);
			} catch (Exception e) {
				// ignore
			}
		}
		return false;
	}

	private void prepareCondition(String[] c, Map<String, String> parameters) {
		for (int i = 0; i < c.length; i++) {
			if (c[i].startsWith("@"))
				c[i] = parameters.get(c[i].substring(1));
		}
	}

	private void fillMapByInformation(Map<String, String> map, String key) throws RemoteException {
		InformationEntryBean info = mCenter.getInformationHandler().getInformation(key);
		for (Field f : info.getClass().getDeclaredFields()) {
			WebField annotation = f.getAnnotation(WebField.class);
			if (annotation != null) {
				f.setAccessible(true);
				try {
					Object value = f.get(info);
					if (value != null)
						map.put(key + "." + annotation.name(), value.toString());
				} catch (IllegalArgumentException | IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}
}