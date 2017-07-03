package de.neo.smarthome.informations;

import java.util.ArrayList;
import java.util.Date;

import de.neo.smarthome.api.IWebInformationUnit.InformationEntryBean;
import de.neo.smarthome.api.IWebInformationUnit.InformationEntryTime;
import de.neo.smarthome.informations.WebInformation.IInformation;

public class InformationTime implements IInformation {

	@Override
	public String getKey() {
		return "time";
	}

	@Override
	public String getDescription() {
		return "Get current time";
	}

	@Override
	public ArrayList<InformationEntryBean> getInformationEntries() {
		InformationEntryTime entry = new InformationEntryTime();
		entry.mMilliseconds = System.currentTimeMillis();
		entry.mExtended = new Date().toString();
		ArrayList<InformationEntryBean> list = new ArrayList<>();
		list.add(entry);
		return list;
	}

}
