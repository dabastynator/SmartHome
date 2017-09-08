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
import de.neo.smarthome.api.Trigger.Parameter;

@Domain
public class Event implements Serializable {

	/**
	 * generated
	 */
	private static final long serialVersionUID = -906066975333495388L;

	@WebField(name = "unit_id")
	@Persist(name = "unitID")
	private String mUnitID;

	@OneToMany(domainClass = Parameter.class, name = "Parameter")
	private List<Parameter> mParameterList = new ArrayList<>();

	@WebField(name = "parameter")
	private Map<String, String> mParameter = new HashMap<String, String>();

	@WebField(name = "condition")
	@Persist(name = "condition")
	private String mCondition;

	public String getUnitID() {
		return mUnitID;
	}

	public void setUnitID(String unitID) {
		mUnitID = unitID;
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

	@OnLoad
	public void onLoad() {
		for (Parameter p : mParameterList)
			mParameter.put(p.mKey, p.mValue);
	}

	public String getCondition() {
		return mCondition;
	}

}
