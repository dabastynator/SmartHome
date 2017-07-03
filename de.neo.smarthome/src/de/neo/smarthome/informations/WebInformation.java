package de.neo.smarthome.informations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebGet;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.api.IWebInformationUnit;

public class WebInformation implements IWebInformationUnit {

	private Map<String, IInformation> mInformations;

	public WebInformation() {
		mInformations = new HashMap<>();
		registerInformation(new InformationTime());
	}

	public void registerInformation(IInformation information) {
		mInformations.put(information.getKey(), information);
	}

	@Override
	@WebRequest(path = "list", description = "Get list of all available informations.", genericClass = InformationBean.class)
	public ArrayList<InformationBean> getInformations() throws RemoteException {
		ArrayList<InformationBean> result = new ArrayList<>();
		for (IInformation i : mInformations.values()) {
			InformationBean bean = new InformationBean();
			bean.mKey = i.getKey();
			bean.mDescription = i.getDescription();
			result.add(bean);
		}
		return result;
	}

	@Override
	@WebRequest(path = "info", description = "Get specific information.", genericClass = InformationEntryBean.class)
	public ArrayList<InformationEntryBean> getInformation(@WebGet(name = "key") String key) throws RemoteException {
		IInformation information = mInformations.get(key);
		if (information == null)
			throw new RemoteException("Unknown information key '" + key + "'");
		return information.getInformationEntries();
	}

	interface IInformation {

		public String getKey();

		public String getDescription();

		public ArrayList<InformationEntryBean> getInformationEntries();
	}

}
