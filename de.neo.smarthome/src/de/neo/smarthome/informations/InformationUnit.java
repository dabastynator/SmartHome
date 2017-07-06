package de.neo.smarthome.informations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.neo.smarthome.api.IWebInformationUnit.InformationEntryBean;

public abstract class InformationUnit {

	protected List<InformationTrigger> mTrigger = new ArrayList<>();

	public abstract String getKey();

	public abstract String getDescription();

	public abstract InformationEntryBean getInformationEntry();

	public void initialize(Element element) throws SAXException, IOException {
		NodeList nodeList = element.getElementsByTagName("Trigger");
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element e = (Element) nodeList.item(i);
			if (e.hasAttribute("triggerID") && e.hasAttribute("onChange")) {
				InformationTrigger trigger = new InformationTrigger();
				trigger.setTrigger(e.getAttribute("triggerID"));
				trigger.setOnChange(e.getAttribute("onChange"));
				mTrigger.add(trigger);
			} else
				throw new SAXException("Trigger node needs triggerID and onChange attribute!");
		}
	}

	public static class InformationTrigger {

		private String mTrigger;

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