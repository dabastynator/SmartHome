package de.neo.smarthome.informations;

import java.util.Date;

import de.neo.smarthome.api.IWebInformationUnit.InformationEntryBean;
import de.neo.smarthome.api.IWebInformationUnit.InformationEntryTime;

public class InformationTime extends InformationUnit {

	@Override
	public String getKey() {
		return "time";
	}

	@Override
	public String getDescription() {
		return "Get current time";
	}

	@Override
	public InformationEntryBean getInformationEntry() {
		InformationEntryTime entry = new InformationEntryTime();
		entry.mMilliseconds = System.currentTimeMillis();
		entry.mExtended = new Date().toString();
		return entry;
	}

}
