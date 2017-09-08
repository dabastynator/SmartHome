package de.neo.smarthome.rccolor;

import java.util.ArrayList;

import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebGet;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.AbstractUnitHandler;
import de.neo.smarthome.SmartHome.ControlUnitFactory;
import de.neo.smarthome.api.IWebLEDStrip;
import de.neo.smarthome.controlcenter.ControlCenter;
import de.neo.smarthome.controlcenter.IControlCenter;
import de.neo.smarthome.controlcenter.IControllUnit;

public class WebLEDStripImpl extends AbstractUnitHandler implements IWebLEDStrip {

	public WebLEDStripImpl(IControlCenter center) {
		super(center);
	}

	@Override
	@WebRequest(path = "list", description = "List all led strips.", genericClass = BeanLEDStrips.class)
	public ArrayList<BeanLEDStrips> getLEDStrips() {
		ArrayList<BeanLEDStrips> result = new ArrayList<>();
		for (IControllUnit unit : mCenter.getControlUnits().values()) {
			try {
				if (unit instanceof RCColorControlUnit) {
					RCColorControlUnit ledStrip = (RCColorControlUnit) unit;
					BeanLEDStrips webLed = new BeanLEDStrips();
					webLed.merge(unit.getWebBean());
					int color = ledStrip.getColor();
					webLed.setRed((color & 0xFF0000) >> 16);
					webLed.setGreen((color & 0x00FF00) >> 8);
					webLed.setBlue((color & 0x0000FF));
					result.add(webLed);
				}
			} catch (RemoteException e) {
			}
		}
		return result;
	}

	@Override
	@WebRequest(path = "setcolor", description = "Set color for specified led strip. Red, green and blue must between 0 and 255.")
	public BeanLEDStrips setColor(@WebGet(name = "id") String id, @WebGet(name = "red") int red,
			@WebGet(name = "green") int green, @WebGet(name = "blue") int blue) {
		if (red < 0 || red > 255)
			throw new IllegalArgumentException("Red componentet must be in [0..255].");
		if (green < 0 || green > 255)
			throw new IllegalArgumentException("Green componentet must be in [0..255].");
		if (blue < 0 || blue > 255)
			throw new IllegalArgumentException("Blue componentet must be in [0..255].");
		int color = (red << 16) | (green << 8) | blue;
		try {
			IControllUnit unit = mCenter.getControlUnit(id);
			if (unit instanceof RCColorControlUnit) {
				RCColorControlUnit ledStrip = (RCColorControlUnit) unit;
				BeanLEDStrips webLed = new BeanLEDStrips();
				webLed.merge(unit.getWebBean());
				ledStrip.setColor(color);
				color = ledStrip.getColor();
				webLed.setRed((color & 0xFF0000) >> 16);
				webLed.setGreen((color & 0x00FF00) >> 8);
				webLed.setBlue((color & 0x0000FF));
				return webLed;
			}
		} catch (RemoteException e) {
		}
		return null;
	}

	@Override
	@WebRequest(path = "setmode", description = "Set mode for specified led strip. 'NormalMode' simply shows the color, 'PartyMode' shows the color elements with strobe effect.")
	public BeanLEDStrips setMode(@WebGet(name = "id") String id, @WebGet(name = "mode") LEDMode mode)
			throws RemoteException {
		try {
			IControllUnit unit = mCenter.getControlUnit(id);
			if (unit instanceof RCColorControlUnit) {
				RCColorControlUnit ledStrip = (RCColorControlUnit) unit;
				BeanLEDStrips webLed = new BeanLEDStrips();
				webLed.merge(unit.getWebBean());
				ledStrip.setMode(mode);
				return webLed;
			}
		} catch (RemoteException e) {
		}
		return null;
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
