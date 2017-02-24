package de.neo.smarthome.controlcenter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.neo.remote.api.RMILogger.LogPriority;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.api.Trigger;
import de.neo.smarthome.cronjob.CronScheduler;

public class CronJobTrigger implements Runnable {

	private ControlCenterImpl mCenter;

	private String mCronDescription;

	private List<Trigger> mTriggerList = new ArrayList<Trigger>();

	protected CronJobTrigger(ControlCenterImpl center) {
		mCenter = center;
	}

	public void schedule() {
		try {
			CronScheduler scheduler = CronScheduler.getInstance();
			scheduler.scheduleJob(this, mCronDescription);
		} catch (ParseException e) {
			RemoteLogger.performLog(LogPriority.ERROR, e.getMessage(),
					"CronJobTrigger");
		}
	}

	public void initialize(Element element) throws SAXException {
		for (String attribute : new String[] { "cronjob" })
			if (!element.hasAttribute(attribute))
				throw new SAXException(attribute + " missing for "
						+ getClass().getSimpleName());
		mCronDescription = element.getAttribute("cronjob");
		for (int i = 0; i < element.getChildNodes().getLength(); i++)
			if (element.getChildNodes().item(i) instanceof Element) {
				Element child = (Element) element.getChildNodes().item(i);
				Trigger trigger = new Trigger();
				trigger.initialize(child);
				mTriggerList.add(trigger);
			}
	}

	@Override
	public String toString() {
		return mTriggerList.toString();
	}

	@Override
	public void run() {
		for (Trigger trigger : mTriggerList)
			mCenter.trigger(trigger);
	}

}
