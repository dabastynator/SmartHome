package de.neo.smarthome.informations;

import de.neo.persist.annotations.Domain;
import de.neo.persist.annotations.Persist;
import de.neo.smarthome.api.IWebInformationUnit.InformationBean;
import de.neo.smarthome.api.IWebInformationUnit.InformationEntryHass;

@Domain()
public class InformationHass extends InformationUnit {

	public static String Key = "InformationHass";
	
	@Persist(name = "hassId")
	private String mHassId;
	
	@Persist(name = "name")
	private String mName;
	
	@Persist(name = "description")
	private String mDescription;
	
	private InformationEntryHass mInfoEntry = new InformationEntryHass();
	
	public String hassId() {
		return mHassId;
	}
	
	@Override
	public String getKey() {
		return mHassId;
	}
	
	@Override
	public String getType() {
		return "hass";
	}

	@Override
	public String getDescription() {
		return "Access information about Home Assisten entity";
	}

	@Override
	public InformationBean getInformationEntry() {
		return mInfoEntry;
	}
	
	public void setState(String state) {
		mInfoEntry.mState = state;
		mInfoEntry.mId = mHassId;
		mInfoEntry.mDescription = mDescription;
		mInfoEntry.mName = mName;
	}

}
