package de.neo.smarthome.informations;

import java.util.Date;

import org.w3c.dom.Element;

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
	public InformationEntryBean getInformationEntry() {
		InformationEntryTime entry = new InformationEntryTime();
		entry.mMilliseconds = System.currentTimeMillis();
		entry.mExtended = new Date().toString();
		return entry;
	}

	@Override
	public void initialize(Element element) {
		// TODO Auto-generated method stub

	}

}
