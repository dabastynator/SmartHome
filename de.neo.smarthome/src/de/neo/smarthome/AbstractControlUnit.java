package de.neo.smarthome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.neo.persist.annotations.OneToMany;
import de.neo.persist.annotations.Persist;
import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.api.Trigger;
import de.neo.smarthome.controlcenter.ControlCenter;
import de.neo.smarthome.controlcenter.IControlCenter;
import de.neo.smarthome.controlcenter.IControlCenter.BeanWeb;
import de.neo.smarthome.controlcenter.IControllUnit;

public abstract class AbstractControlUnit implements IControllUnit {

	@Persist(name = "name")
	protected String mName;

	@Persist(name = "type")
	protected String mDescription;

	@Persist
	protected float x, y, z;

	@Persist(name = "id")
	protected String mID;

	@OneToMany(domainClass = Trigger.class, name = "Trigger")
	protected List<Trigger> mTrigger = new ArrayList<Trigger>();

	private IControlCenter mCenter;

	@Override
	public void setControlCenter(ControlCenter center) {
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
		return new float[] { x, y, z };
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
				try {
					mCenter.trigger(fireTrigger);
				} catch (RemoteException e) {
				}
			}
		}
	}

	@Override
	public BeanWeb getWebBean() {
		BeanWeb bean = new BeanWeb();
		bean.setID(mID);
		bean.setName(mName);
		bean.setDescription(mDescription);
		bean.setX(x);
		bean.setY(y);
		bean.setZ(z);
		return bean;
	}
}
