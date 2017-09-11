package de.neo.smarthome.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.neo.persist.annotations.Domain;
import de.neo.persist.annotations.OnLoad;
import de.neo.persist.annotations.OneToMany;
import de.neo.persist.annotations.Persist;
import de.neo.remote.web.WebField;

@Domain
public class Trigger implements Serializable {

	public static final String CLIENT_ACTION = "trigger.client_action";

	/**
	 * generated
	 */
	private static final long serialVersionUID = -906066975333495388L;

	@Persist(name = "triggerID")
	private String mTriggerID;

	@OneToMany(domainClass = Parameter.class, name = "Parameter")
	private List<Parameter> mParameterList = new ArrayList<>();

	private Map<String, String> mParameter = new HashMap<>();

	public Trigger(Trigger tigger) {
		mTriggerID = tigger.getTriggerID();
		mParameter.putAll(tigger.getParameter());
	}

	public Trigger() {
	}

	@OnLoad
	public void onLoad() {
		for (Parameter p : mParameterList)
			mParameter.put(p.mKey, p.mValue);
	}

	public String getTriggerID() {
		return mTriggerID;
	}

	public void setTriggerID(String triggerID) {
		mTriggerID = triggerID;
	}

	public Map<String, String> getParameter() {
		return mParameter;
	}

	public void putParameter(String key, String value) {
		mParameter.put(key, value);
	}

	public void putParameter(String key, int value) {
		mParameter.put(key, String.valueOf(value));
	}

	public void putParameter(String key, double value) {
		mParameter.put(key, String.valueOf(value));
	}

	public String getParameter(String key) {
		return mParameter.get(key);
	}

	public int getIntParameter(String key) {
		return Integer.parseInt(mParameter.get(key));
	}

	public double getDoubleParameter(String key) {
		return Double.parseDouble(mParameter.get(key));
	}

	@Override
	public String toString() {
		return mTriggerID;
	}

	@Domain
	public static class Parameter {

		@WebField(name = "key")
		@Persist(name = "key")
		public String mKey;

		@WebField(name = "value")
		@Persist(name = "value")
		public String mValue;
	}

}
