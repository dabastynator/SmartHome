package de.neo.remote.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Trigger implements Serializable {

	public static final String CLIENT_ACTION = "trigger.client_action";

	/**
	 * generated
	 */
	private static final long serialVersionUID = -906066975333495388L;

	private String mControlUnitId;

	private String mTriggerID;

	private Map<String, String> mParameter = new HashMap<String, String>();

	public Trigger(Trigger tigger) {
		mControlUnitId = tigger.getControlUnitId();
		mTriggerID = tigger.getTriggerID();
		mParameter.putAll(tigger.getParameter());
	}

	public Trigger() {
	}

	public String getControlUnitId() {
		return mControlUnitId;
	}

	public void setControlUnitId(String controlUnitId) {
		mControlUnitId = controlUnitId;
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

	public void initialize(Element element) throws SAXException {
		for (String attribute : new String[] { "triggerID" })
			if (!element.hasAttribute(attribute))
				throw new SAXException(attribute + " missing for " + getClass().getSimpleName());
		setTriggerID(element.getAttribute("triggerID"));
		NodeList paramterNodes = element.getChildNodes();
		for (int j = 0; j < paramterNodes.getLength(); j++) {
			if (paramterNodes.item(j) instanceof Element) {
				Element parameter = (Element) paramterNodes.item(j);
				if (parameter.getNodeName().equals("Parameter")) {
					for (String attribute : new String[] { "key", "value" })
						if (!parameter.hasAttribute(attribute))
							throw new SAXException(attribute + " missing for " + getClass().getSimpleName());
					putParameter(parameter.getAttribute("key").toLowerCase(),
							parameter.getAttribute("value").toLowerCase());
				}
			}
		}
	}

	@Override
	public String toString() {
		return mTriggerID;
	}

}
