package de.neo.smarthome.api;

import java.io.Serializable;
import java.util.ArrayList;

import de.neo.remote.rmi.RemoteAble;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebField;
import de.neo.remote.web.WebGet;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.api.IWebInformationUnit.InformationEntryBean;

/**
 * The Command-Action represents a action to execute remoteable. The action
 * executes a simple command line with parameter.
 * 
 * @author sebastian
 *
 */
public interface IWebInformationUnit extends RemoteAble {

	/**
	 * Get list of all available informations.
	 * 
	 * @return list of informations
	 * @throws RemoteException
	 */
	@WebRequest(path = "list", description = "Get list of all available informations.", genericClass = InformationBean.class)
	public ArrayList<InformationBean> getInformations() throws RemoteException;

	/**
	 * Get specific information.
	 * 
	 * @param key
	 * @return
	 * @throws RemoteException
	 */
	@WebRequest(path = "info", description = "Get specific information.", genericClass = InformationEntryBean.class)
	public ArrayList<InformationEntryBean> getInformation(@WebGet(name = "key") String key) throws RemoteException;

	public static class InformationBean implements Serializable {

		private static final long serialVersionUID = -5087796530525333429L;

		@WebField(name = "key")
		public String mKey;

		@WebField(name = "description")
		public String mDescription;
	}

	public static class InformationEntryBean implements Serializable {

		private static final long serialVersionUID = -5641533643847316616L;

		public InformationEntryBean() {
			mClass = getClass().getName();
		}

		@WebField(name = "class")
		private String mClass;

	}

	public static class InformationEntryTime extends InformationEntryBean {

		@WebField(name = "milliseconds")
		public long mMilliseconds;

		@WebField(name = "extended")
		public String mExtended;

	}
}
