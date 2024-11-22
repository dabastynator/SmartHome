package de.neo.smarthome.api;

import java.io.Serializable;
import java.util.ArrayList;

import de.neo.remote.rmi.RemoteAble;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebField;
import de.neo.remote.web.WebParam;
import de.neo.remote.web.WebRequest;

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
	@WebRequest(path = "info", description = "Get specific information.")
	public InformationBean getInformation(@WebParam(name = "key") String key) throws RemoteException;

	public static class InformationBean implements Serializable {

		private static final long serialVersionUID = -5641533643847316616L;

		@WebField(name = "type")
		public String mType;
		
		@WebField(name = "key")
		public String mKey;
		
	}

	public static class InformationEntryTime extends InformationBean {

		@WebField(name = "milliseconds")
		public long mMilliseconds;

		@WebField(name = "extended")
		public String mExtended;

	}

	public static class InformationEntryWeather extends InformationBean {

		public enum WeatherSun {
			Day, Night
		};

		@WebField(name = "day_night")
		public WeatherSun mDayNight;

		@WebField(name = "rain")
		public boolean mRain;

		@WebField(name = "snow")
		public boolean mSnow;

		@WebField(name = "clouds")
		public int mClouds;

		@WebField(name = "celsius")
		public double mCelsius;
	}
	
	public static class InformationEntryHass extends InformationBean {
		
		@WebField(name = "name")
		public String mName;
		
		@WebField(name = "description")
		public String mDescription;
		
		@WebField(name = "id")
		public String mId;
		
		@WebField(name = "state")
		public String mState;
	}
}
