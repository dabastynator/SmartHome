package de.neo.smarthome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.neo.persist.annotations.OneToMany;
import de.neo.persist.annotations.Persist;
import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.api.BeanWeb;
import de.neo.smarthome.api.IControllUnit;
import de.neo.smarthome.api.Trigger;
import de.neo.smarthome.controlcenter.ControlCenter;

public abstract class AbstractControlUnit implements IControllUnit {

	@Persist(name = "name")
	protected String mName;

	@Persist(name = "id")
	protected String mID;

	@OneToMany(domainClass = Trigger.class, name = "Trigger")
	protected List<Trigger> mTrigger = new ArrayList<Trigger>();

	protected ControlCenter mCenter;

	public void setName(String name) {
		mName = name;
	}

	public void setId(String id) {
		mID = id;
	}

	@Override
	public void setControlCenter(ControlCenter center) {
		mCenter = center;
	}

	@Override
	public String getName() throws RemoteException {
		return mName;
	}

	@Override
	public String getID() throws RemoteException {
		return mID;
	}

	public void fireTrigger(Map<String, String> parameterExchange, String condition) {
		for (Trigger trigger : mTrigger) {
			String triggerCondition = trigger.getParameter("condition");
			if (triggerCondition == null || condition.equalsIgnoreCase(triggerCondition)) {
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
				mCenter.trigger(fireTrigger);
			}
		}
	}

	@Override
	public BeanWeb getWebBean() {
		BeanWeb bean = new BeanWeb();
		bean.setID(mID);
		bean.setName(mName);
		return bean;
	}
}
