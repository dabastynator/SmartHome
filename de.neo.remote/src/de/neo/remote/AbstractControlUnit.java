package de.neo.remote;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.neo.remote.api.IControlCenter;
import de.neo.remote.api.IControlUnit;
import de.neo.remote.api.Trigger;
import de.neo.rmi.protokol.RemoteException;

public abstract class AbstractControlUnit implements IControlUnit {

	protected String mName;
	protected String mDescription;
	protected float[] mPosition;
	protected String mID;
	protected List<Trigger> mTrigger = new ArrayList<Trigger>();
	private IControlCenter mCenter;

	public AbstractControlUnit(IControlCenter center) {
		mCenter = center;
	}

	@Override
	public String getName() throws RemoteException {
		return mName;
	}

	@Override
	public String getDescription() throws RemoteException {
		return mDescription;
	}

	@Override
	public float[] getPosition() throws RemoteException {
		return mPosition;
	}

	@Override
	public String getID() throws RemoteException {
		return mID;
	}

	public void initialize(Element element) throws SAXException, IOException {
		for (String attribute : new String[] { "id", "name", "type", "x", "y",
				"z" })
			if (!element.hasAttribute(attribute))
				throw new SAXException(attribute + " missing for "
						+ getClass().getSimpleName());
		mID = element.getAttribute("id");
		mName = element.getAttribute("name");
		mDescription = element.getAttribute("type");
		mPosition = new float[] { Float.parseFloat(element.getAttribute("x")),
				Float.parseFloat(element.getAttribute("y")),
				Float.parseFloat(element.getAttribute("z")) };
		NodeList childNodes = element.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			if (childNodes.item(i) instanceof Element) {
				Element child = (Element) childNodes.item(i);
				if (child.getNodeName().equals("Trigger")) {
					Trigger trigger = new Trigger();
					trigger.initialize(child);
					mTrigger.add(trigger);
				}
			}
		}
	}

	public void fireTrigger(Map<String, String> parameterExchange,
			String condition) {
		for (Trigger trigger : mTrigger) {
			String triggerCondition = trigger.getParameter("condition");
			if (triggerCondition == null
					|| condition.equalsIgnoreCase(triggerCondition)) {
				Trigger fireTrigger = new Trigger(trigger);
				if (parameterExchange != null) {
					Map<String, String> exchange = new HashMap<String, String>();
					for (String key : trigger.getParameter().keySet()) {
						String value = trigger.getParameter(key);
						if (parameterExchange.containsKey(value))
							exchange.put(key, parameterExchange.get(value));
					}
					fireTrigger.getParameter().putAll(exchange);
				}
				try {
					mCenter.trigger(fireTrigger);
				} catch (RemoteException e) {
				}
			}
		}
	}
}
