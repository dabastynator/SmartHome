package de.neo.smarthome.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.neo.remote.api.WebField;

public class Event implements Serializable {

	/**
	 * generated
	 */
	private static final long serialVersionUID = -906066975333495388L;

	@WebField(name = "unit_id")
	private String mUnitID;

	@WebField(name = "parameter")
	private Map<String, String> mParameter = new HashMap<String, String>();

	public Event(Event event) {
		mUnitID = event.getUnitID();
		mParameter.putAll(event.getParameter());
	}

	public Event() {
	}

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

	public void initialize(Element element) throws SAXException {
		for (String attribute : new String[] { "unitID" })
			if (!element.hasAttribute(attribute))
				throw new SAXException(attribute + " missing for " + getClass().getSimpleName());
		setUnitID(element.getAttribute("unitID"));
		NodeList paramterNodes = element.getChildNodes();
		for (int j = 0; j < paramterNodes.getLength(); j++) {
			if (paramterNodes.item(j) instanceof Element) {
				Element parameter = (Element) paramterNodes.item(j);
				if (parameter.getNodeName().equals("Parameter")) {
					for (String attribute : new String[] { "key", "value" })
						if (!parameter.hasAttribute(attribute))
							throw new SAXException(attribute + " missing for " + getClass().getSimpleName());
					putParameter(parameter.getAttribute("key").toLowerCase(), parameter.getAttribute("value"));
				}
			}
		}
	}

}
