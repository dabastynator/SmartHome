package de.neo.remote.rccolor;

import java.util.ArrayList;

import de.neo.remote.AbstractUnitHandler;
import de.neo.remote.api.IControlCenter;
import de.neo.remote.api.IControlUnit;
import de.neo.remote.api.IRCColor;
import de.neo.remote.api.IWebLEDStrip;
import de.neo.rmi.api.WebGet;
import de.neo.rmi.api.WebRequest;
import de.neo.rmi.protokol.RemoteException;

public class WebLEDStripImpl extends AbstractUnitHandler implements IWebLEDStrip {

	public WebLEDStripImpl(IControlCenter center) {
		super(center);
	}

	@Override
	@WebRequest(path = "list", description = "List all led strips.", genericClass = BeanLEDStrips.class)
	public ArrayList<BeanLEDStrips> getLEDStrips() {
		ArrayList<BeanLEDStrips> result = new ArrayList<>();
		for (IControlUnit unit : mCenter.getControlUnits().values()) {
			try {
				if (unit.getRemoteableControlObject() instanceof IRCColor) {
					IRCColor ledStrip = (IRCColor) unit.getRemoteableControlObject();
					BeanLEDStrips webLed = new BeanLEDStrips();
					unit.config(webLed);
					int color = ledStrip.getColor();
					webLed.setRed((color & 0xFF0000) >> 16);
					webLed.setGreen((color & 0x00FF00) >> 8);
					webLed.setBlue((color & 0x0000FF));
					unit.config(webLed);
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
			IControlUnit unit = mCenter.getControlUnit(id);
			if (unit.getRemoteableControlObject() instanceof IRCColor) {
				IRCColor ledStrip = (IRCColor) unit.getRemoteableControlObject();
				BeanLEDStrips webLed = new BeanLEDStrips();
				unit.config(webLed);
				ledStrip.setColor(color);
				color = ledStrip.getColor();
				webLed.setRed((color & 0xFF0000) >> 16);
				webLed.setGreen((color & 0x00FF00) >> 8);
				webLed.setBlue((color & 0x0000FF));
				unit.config(webLed);
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

}
