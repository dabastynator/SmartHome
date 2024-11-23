package de.neo.smarthome.rccolor;

import java.util.ArrayList;

import de.neo.persist.Dao;
import de.neo.persist.DaoException;
import de.neo.persist.DaoFactory;
import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebParam;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.AbstractUnitHandler;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.SmartHome.ControlUnitFactory;
import de.neo.smarthome.api.IControllUnit;
import de.neo.smarthome.api.IWebLEDStrip;
import de.neo.smarthome.controlcenter.ControlCenter;
import de.neo.smarthome.user.User;
import de.neo.smarthome.user.User.UserRole;
import de.neo.smarthome.user.UserSessionHandler;

public class WebLEDStripImpl extends AbstractUnitHandler implements IWebLEDStrip {

	public WebLEDStripImpl(ControlCenter center) {
		super(center);
	}

	private BeanLEDStrips toLEDBean(RCColorControlUnit unit) {
		BeanLEDStrips webLed = new BeanLEDStrips();
		webLed.merge(unit.getWebBean());
		int color = unit.getColor();
		webLed.setRed((color & 0xFF0000) >> 16);
		webLed.setGreen((color & 0x00FF00) >> 8);
		webLed.setBlue((color & 0x0000FF));
		webLed.setMode(unit.getMode());
		return webLed;
	}

	@Override
	@WebRequest(path = "list", description = "List all led strips.", genericClass = BeanLEDStrips.class)
	public ArrayList<BeanLEDStrips> getLEDStrips(@WebParam(name = "token") String token) throws RemoteException {
		User user = UserSessionHandler.require(token);
		ArrayList<BeanLEDStrips> result = new ArrayList<>();
		for (IControllUnit unit : mCenter.getAccessHandler().unitsFor(user)) {
			if (unit instanceof RCColorControlUnit) {
				RCColorControlUnit ledStrip = (RCColorControlUnit) unit;
				result.add(toLEDBean(ledStrip));
			}
		}
		return result;
	}

	@Override
	@WebRequest(path = "setcolor", description = "Set color for specified led strip. Red, green and blue must between 0 and 255.")
	public BeanLEDStrips setColor(@WebParam(name = "token") String token, @WebParam(name = "id") String id,
			@WebParam(name = "red") int red, @WebParam(name = "green") int green, @WebParam(name = "blue") int blue)
			throws RemoteException {
		RCColorControlUnit ledStrip = mCenter.getAccessHandler().require(token, id);
		if (red < 0 || red > 255)
			throw new IllegalArgumentException("Red componentet must be in [0..255].");
		if (green < 0 || green > 255)
			throw new IllegalArgumentException("Green componentet must be in [0..255].");
		if (blue < 0 || blue > 255)
			throw new IllegalArgumentException("Blue componentet must be in [0..255].");
		int color = (red << 16) | (green << 8) | blue;
		ledStrip.setColor(color);
		return toLEDBean(ledStrip);
	}

	@Override
	@WebRequest(path = "setmode", description = "Set mode for specified led strip. 'NormalMode' simply shows the color, 'PartyMode' shows the color elements with strobe effect.")
	public BeanLEDStrips setMode(@WebParam(name = "token") String token, @WebParam(name = "id") String id,
			@WebParam(name = "mode") LEDMode mode) throws RemoteException {
		RCColorControlUnit ledStrip = mCenter.getAccessHandler().require(token, id);
		ledStrip.setMode(mode);
		return toLEDBean(ledStrip);
	}

	@WebRequest(path = "create", description = "Create new LED strip.")
	public BeanLEDStrips createNew(@WebParam(name = "token") String token, @WebParam(name = "id") String id,
			@WebParam(name = "name") String name, @WebParam(name = "description") String description,
			@WebParam(name = "x") float x, @WebParam(name = "y") float y, @WebParam(name = "z") float z)
			throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		RCColorControlUnit ledUnit = new RCColorControlUnit();
		if (mCenter.getControlUnit(id) != null) {
			throw new RemoteException("Unit with id " + id + " already exists");
		}
		ledUnit.setName(name);
		ledUnit.setId(id);
		Dao<RCColorControlUnit> dao = DaoFactory.getInstance().getDao(RCColorControlUnit.class);
		dao.save(ledUnit);
		mCenter.addControlUnit(ledUnit);
		RemoteLogger.performLog(LogPriority.INFORMATION, "Create new led strip " + ledUnit.getName(), "WebLEDStrip");
		return toLEDBean(ledUnit);
	}

	@WebRequest(path = "update", description = "Update existing LED strip.")
	public BeanLEDStrips updateExisting(@WebParam(name = "token") String token, @WebParam(name = "id") String id,
			@WebParam(name = "name") String name, @WebParam(name = "description") String description,
			@WebParam(name = "x") float x, @WebParam(name = "y") float y, @WebParam(name = "z") float z)
			throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		IControllUnit unit = mCenter.getControlUnit(id);
		if (!(unit instanceof RCColorControlUnit)) {
			throw new RemoteException("Unknown led strip " + id);
		}
		RCColorControlUnit ledUnit = (RCColorControlUnit) unit;
		ledUnit.setName(name);
		Dao<RCColorControlUnit> dao = DaoFactory.getInstance().getDao(RCColorControlUnit.class);
		dao.update(ledUnit);
		RemoteLogger.performLog(LogPriority.INFORMATION, "Update existing led strip " + ledUnit.getName(),
				"WebLEDStrip");
		return toLEDBean(ledUnit);
	}

	@WebRequest(path = "delete", description = "Delete LED strip.")
	public void delete(@WebParam(name = "token") String token, @WebParam(name = "id") String id)
			throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		IControllUnit unit = mCenter.getControlUnit(id);
		if (!(unit instanceof RCColorControlUnit)) {
			throw new RemoteException("Unknown LED strip " + id);
		}
		RCColorControlUnit ledUnit = (RCColorControlUnit) unit;
		Dao<RCColorControlUnit> dao = DaoFactory.getInstance().getDao(RCColorControlUnit.class);
		dao.delete(ledUnit);
		mCenter.removeControlUnit(ledUnit);
		RemoteLogger.performLog(LogPriority.INFORMATION, "Remove led strip " + ledUnit.getName(), "WebLEDStrip");
	}

	@Override
	public String getWebPath() {
		return "ledstrip";
	}

	public static class LEDStripFactory implements ControlUnitFactory {

		@Override
		public Class<?> getUnitClass() {
			return RCColorControlUnit.class;
		}

		@Override
		public AbstractUnitHandler createUnitHandler(ControlCenter center) {
			return new WebLEDStripImpl(center);
		}

	}
}
