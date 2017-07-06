package de.neo.smarthome.informations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebGet;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.api.IWebInformationUnit;
import de.neo.smarthome.informations.InformationWeather.InformationWeatherFactory;

public class WebInformation implements IWebInformationUnit {

	private Map<String, InformationUnit> mInformations;
	private Map<String, IInformationFactory> mFactories;

	public WebInformation() {
		mInformations = new HashMap<>();
		mFactories = new HashMap<>();
		mFactories.put(InformationWeather.Key, new InformationWeatherFactory());
		registerInformation(new InformationTime());
	}

	public void registerInformation(InformationUnit information) {
		mInformations.put(information.getKey(), information);
	}

	@Override
	@WebRequest(path = "list", description = "Get list of all available informations.", genericClass = InformationBean.class)
	public ArrayList<InformationBean> getInformations() throws RemoteException {
		ArrayList<InformationBean> result = new ArrayList<>();
		for (InformationUnit i : mInformations.values()) {
			InformationBean bean = new InformationBean();
			bean.mKey = i.getKey();
			bean.mDescription = i.getDescription();
			result.add(bean);
		}
		return result;
	}

	@Override
	@WebRequest(path = "info", description = "Get specific information.")
	public InformationEntryBean getInformation(@WebGet(name = "key") String key) throws RemoteException {
		InformationUnit information = mInformations.get(key);
		if (information == null)
			throw new RemoteException("Unknown information key '" + key + "'");
		return information.getInformationEntry();
	}

	interface IInformationFactory {
		public String getKey();

		public InformationUnit createInformation();
	}

	public void initialize(Document doc) throws SAXException, IOException {
		for (IInformationFactory factory : mFactories.values()) {
			NodeList nodeList = doc.getElementsByTagName(factory.getKey());
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element element = (Element) nodeList.item(i);
				InformationUnit info = factory.createInformation();
				info.initialize(element);
				registerInformation(info);
			}
		}
	}

}
