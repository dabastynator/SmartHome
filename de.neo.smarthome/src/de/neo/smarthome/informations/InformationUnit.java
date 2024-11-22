package de.neo.smarthome.informations;

import java.util.ArrayList;
import java.util.List;

import de.neo.persist.annotations.Domain;
import de.neo.persist.annotations.OneToMany;
import de.neo.persist.annotations.Persist;
import de.neo.smarthome.api.IWebInformationUnit.InformationBean;

public abstract class InformationUnit {

	@OneToMany(domainClass = InformationTrigger.class, name = "Trigger")
	protected List<InformationTrigger> mTrigger = new ArrayList<>();

	public abstract String getKey();
	
	public String getType() {
		return getKey();
	}

	public abstract String getDescription();

	public abstract InformationBean getInformationEntry();

	@Domain
	public static class InformationTrigger {

		@Persist(name = "triggerID")
		private String mTrigger;

		@Persist(name = "onChange")
		private String mOnChange;

		public String getTrigger() {
			return mTrigger;
		}

		public void setTrigger(String trigger) {
			mTrigger = trigger;
		}

		public String getOnChange() {
			return mOnChange;
		}

		public void setOnChange(String onChange) {
			mOnChange = onChange;
		}
	}
}