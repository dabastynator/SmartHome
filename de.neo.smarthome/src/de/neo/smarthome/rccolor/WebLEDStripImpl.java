package de.neo.smarthome.rccolor;

import java.util.ArrayList;

import de.neo.persist.Dao;
import de.neo.persist.DaoException;
import de.neo.persist.DaoFactory;
import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebGet;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.AbstractUnitHandler;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.SmartHome.ControlUnitFactory;
import de.neo.smarthome.api.IControlCenter;
import de.neo.smarthome.api.IControllUnit;
import de.neo.smarthome.api.IWebLEDStrip;
import de.neo.smarthome.controlcenter.ControlCenter;
import de.neo.smarthome.user.User;
import de.neo.smarthome.user.User.UserRole;
import de.neo.smarthome.user.UserSessionHandler;

public class WebLEDStripImpl extends AbstractUnitHandler implements IWebLEDStrip {

	public WebLEDStripImpl(IControlCenter center) {
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
	public ArrayList<BeanLEDStrips> getLEDStrips(@WebGet(name = "token") String token) throws RemoteException {
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
	public BeanLEDStrips setColor(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "red") int red, @WebGet(name = "green") int green, @WebGet(name = "blue") int blue)
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
	public BeanLEDStrips setMode(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "mode") LEDMode mode) throws RemoteException {
		RCColorControlUnit ledStrip = mCenter.getAccessHandler().require(token, id);
		ledStrip.setMode(mode);
		return toLEDBean(ledStrip);
	}

	@WebRequest(path = "create", description = "Create new LED strip.")
	public BeanLEDStrips createNew(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "name") String name, @WebGet(name = "description") String description,
			@WebGet(name = "x") float x, @WebGet(name = "y") float y, @WebGet(name = "z") float z)
			throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		RCColorControlUnit ledUnit = new RCColorControlUnit();
		if (mCenter.getControlUnit(id) != null) {
			throw new RemoteException("Unit with id " + id + " already exists");
		}
		ledUnit.setName(name);
		ledUnit.setDescription(description);
		ledUnit.setPosition(x, y, z);
		ledUnit.setId(id);
		Dao<RCColorControlUnit> dao = DaoFactory.getInstance().getDao(RCColorControlUnit.class);
		dao.save(ledUnit);
		mCenter.addControlUnit(ledUnit);
		RemoteLogger.performLog(LogPriority.INFORMATION, "Create new led strip " + ledUnit.getName(), "WebLEDStrip");
		return toLEDBean(ledUnit);
	}

	@WebRequest(path = "update", description = "Update existing LED strip.")
	public BeanLEDStrips updateExisting(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "name") String name, @WebGet(name = "description") String description,
			@WebGet(name = "x") float x, @WebGet(name = "y") float y, @WebGet(name = "z") float z)
			throws RemoteException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		IControllUnit unit = mCenter.getControlUnit(id);
		if (!(unit instanceof RCColorControlUnit)) {
			throw new RemoteException("Unknown led strip " + id);
		}
		RCColorControlUnit ledUnit = (RCColorControlUnit) unit;
		ledUnit.setName(name);
		ledUnit.setDescription(description);
		ledUnit.setPosition(x, y, z);
		Dao<RCColorControlUnit> dao = DaoFactory.getInstance().getDao(RCColorControlUnit.class);
		dao.update(ledUnit);
		RemoteLogger.performLog(LogPriority.INFORMATION, "Update existing led strip " + ledUnit.getName(),
				"WebLEDStrip");
		return toLEDBean(ledUnit);
	}

	@WebRequest(path = "delete", description = "Delete LED strip.")
	public void delete(@WebGet(name = "token") String token, @WebGet(name = "id") String id)
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
