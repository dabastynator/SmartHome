package de.neo.smarthome.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import de.neo.persist.DaoException;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebField;
import de.neo.smarthome.controlcenter.CronJobTrigger;
import de.neo.smarthome.user.UnitAccessHandler;

/**
 * The control center handles all control units and information about the
 * controlled object.
 * 
 * @author sebastian
 * 
 */
public interface IControlCenter {

	/**
	 * Add new remote control unit.
	 * 
	 * @param controlUnit
	 * @throws RemoteException
	 */
	public void addControlUnit(IControllUnit controlUnit) throws RemoteException;


	/**
	 * remove remote control unit.
	 * 
	 * @param controlUnit
	 * @throws RemoteException
	 */
	public void removeControlUnit(IControllUnit controlUnit) throws RemoteException;

	/**
	 * Get control unit by specified unit-id.
	 * 
	 * @param id
	 * @return control unit by number
	 * @throws RemoteException
	 */
	public IControllUnit getControlUnit(String id) throws RemoteException;

	public UnitAccessHandler getAccessHandler();

	public IWebInformationUnit getInformationHandler();

	/**
	 * Trigger a trigger. The trigger is specified by the parameter. Scripts are
	 * used to map events for control unit.
	 * 
	 * @param trigger
	 * @return number of triggered events
	 * @throws RemoteException
	 */
	public int trigger(Trigger trigger);

	/**
	 * Get all controlunits of the controlcenter. The map maps the control-unit id
	 * to the object.
	 * 
	 * @return map of controlunits
	 */
	public Map<String, IControllUnit> getControlUnits();

	public List<Script> getScripts();

	public Script getScript(String id);

	public void addScript(Script script) throws DaoException;

	public void deleteScript(String triggerID) throws DaoException;

	public List<CronJobTrigger> getCronTriggers();

	public CronJobTrigger getCronTrigger(long id) throws DaoException;

	public void addCronTrigger(CronJobTrigger trigger) throws DaoException;

	public void deleteCronTrigger(CronJobTrigger trigger) throws DaoException;

	public static class BeanWeb implements Serializable {

		private static final long serialVersionUID = -4066506544238955935L;

		@WebField(name = "name")
		private String mName;

		@WebField(name = "id")
		private String mID;

		public String getName() {
			return mName;
		}

		public void setName(String name) {
			mName = name;
		}

		public String getID() {
			return mID;
		}

		public void setID(String iD) {
			mID = iD;
		}

		public void merge(BeanWeb webBean) {
			mID = webBean.getID();
			mName = webBean.getName();
		}

	}

}
