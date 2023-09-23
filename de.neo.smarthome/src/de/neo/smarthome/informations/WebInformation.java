package de.neo.smarthome.informations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.neo.persist.Dao;
import de.neo.persist.DaoException;
import de.neo.persist.DaoFactory;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebParam;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.api.IWebInformationUnit;

public class WebInformation implements IWebInformationUnit {

	private static final Class<?>[] InformationClasses = new Class<?>[] { InformationTime.class,
			InformationWeather.class };

	private Map<String, InformationUnit> mInformations = new HashMap<>();

	public WebInformation() {
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
	public InformationEntryBean getInformation(@WebParam(name = "key") String key) throws RemoteException {
		InformationUnit information = mInformations.get(key);
		if (information == null)
			throw new RemoteException("Unknown information key '" + key + "'");
		return information.getInformationEntry();
	}

	public void initialize() throws DaoException {
		for (Class<?> infoClass : InformationClasses) {
			Dao<InformationUnit> dao = DaoFactory.getInstance().getDao(infoClass);
			if (dao != null) {
				for (InformationUnit info : dao.loadAll()) {
					registerInformation(info);
				}
			}
		}
	}

}
