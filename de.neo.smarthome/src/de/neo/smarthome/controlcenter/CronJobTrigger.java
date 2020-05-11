package de.neo.smarthome.controlcenter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import de.neo.persist.annotations.Domain;
import de.neo.persist.annotations.Id;
import de.neo.persist.annotations.OneToMany;
import de.neo.persist.annotations.Persist;
import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.api.IControlCenter;
import de.neo.smarthome.api.Trigger;
import de.neo.smarthome.cronjob.CronScheduler;

@Domain
public class CronJobTrigger implements Runnable {

	private IControlCenter mCenter;

	@Id(name = "id")
	private long mId;

	@Persist(name = "cronjob")
	private String mCronDescription;

	@OneToMany(domainClass = Trigger.class, name = "Trigger")
	private List<Trigger> mTriggerList = new ArrayList<Trigger>();

	public void setControlCenter(IControlCenter center) {
		mCenter = center;
	}

	public void schedule() {
		try {
			CronScheduler scheduler = CronScheduler.getInstance();
			scheduler.scheduleJob(this, mCronDescription);
		} catch (ParseException e) {
			RemoteLogger.performLog(LogPriority.ERROR, e.getMessage(), "CronJobTrigger");
		}
	}

	@Override
	public String toString() {
		return mTriggerList.toString();
	}

	public String getCronDescription() {
		return mCronDescription;
	}

	public void setCronDescription(String cronDescription) {
		mCronDescription = cronDescription;
	}

	public List<Trigger> getTriggerList() {
		return mTriggerList;
	}

	public void setTriggerList(List<Trigger> triggerList) {
		mTriggerList = triggerList;
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		mId = id;
	}

	@Override
	public void run() {
		for (Trigger trigger : mTriggerList)
			mCenter.trigger(trigger);
	}

}
