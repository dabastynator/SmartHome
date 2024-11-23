package de.neo.smarthome.api;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.neo.persist.annotations.Domain;
import de.neo.persist.annotations.OneToMany;
import de.neo.persist.annotations.Persist;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebField;
import de.neo.smarthome.api.IWebInformationUnit.InformationBean;
import de.neo.smarthome.controlcenter.ControlCenter;

@Domain
public class Script {

	@WebField(name = "events")
	@OneToMany(domainClass = Event.class, name = "Event")
	private List<Event> mEvents = new ArrayList<Event>();

	@WebField(name = "trigger")
	@Persist(name = "trigger")
	private String mTrigger;

	@WebField(name = "information")
	@Persist(name = "information")
	private String mInformation;

	private ControlCenter mCenter;

	public String getTriggerID() {
		return mTrigger;
	}

	public void setTriggerID(String triggerID) {
		mTrigger = triggerID;
	}

	public void setControlcenter(ControlCenter center) {
		mCenter = center;
	}

	public Event[] getEventsForTrigger(Trigger trigger) throws RemoteException {
		boolean fireEvents = trigger.getTriggerID().equals(mTrigger);
		if (fireEvents) {
			Map<String, String> parameters = new HashMap<>();
			parameters.putAll(trigger.getParameter());
			if (mInformation != null) {
				for (String info : mInformation.split(",")) {
					info = info.trim();
					if (info.length() > 0)
						fillMapByInformation(parameters, info);
				}
			}
			List<Event> events = new ArrayList<>();
			for (Event e : mEvents) {
				if (evaluateCondition(parameters, e.getCondition()))
					events.add(e);
			}
			return events.toArray(new Event[events.size()]);
		}
		return new Event[0];

	}

	private boolean evaluateCondition(Map<String, String> parameters, String condition) {
		if (condition == null || condition.trim().length() == 0)
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
		InformationBean info = mCenter.getInformationHandler().getInformation(key);
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

	public List<Event> getEvents() {
		return mEvents;
	}

	public String getInformations() {
		return mInformation;
	}

	public void setInformation(String informations) {
		mInformation = informations;
	}
}