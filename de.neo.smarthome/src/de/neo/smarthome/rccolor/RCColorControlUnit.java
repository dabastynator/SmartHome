package de.neo.smarthome.rccolor;

import de.neo.persist.annotations.Domain;
import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.AbstractControlUnit;
import de.neo.smarthome.api.Event;
import de.neo.smarthome.api.IWebLEDStrip.LEDMode;
import de.neo.smarthome.gpio.GPIOSender;

@Domain(name = "ColorSetter")
public class RCColorControlUnit extends AbstractControlUnit {

	private int mCurrentColor;

	private LEDMode mMode;

	private GPIOSender mSender = GPIOSender.getInstance();

	@Override
	public boolean performEvent(Event event) throws RemoteException, EventException {
		String colorString = event.getParameter("color");
		if (colorString == null)
			throw new EventException("Parameter color missing to set the color!");
		int color = 0, duration = 0;
		try {
			color = Integer.parseInt(colorString);
			String durationString = event.getParameter("duration");
			if (durationString != null)
				duration = Integer.parseInt(durationString);
		} catch (Exception e) {
			throw new EventException("Cannot parse color code or duration");
		}
		if (duration == 0)
			setColor(color);
		else
			setColor(color, duration);
		return true;
	}

	public void setColor(int color) {
		mCurrentColor = color;
		mSender.setColor(mCurrentColor);
	}

	public void setColor(int color, int duration) {
		mSender.setColor(color);
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mSender.setColor(mCurrentColor);
	}

	public int getColor() {
		return mCurrentColor;
	}

	public void setMode(LEDMode mode) {
		mSender.setMode(mode);
		mMode = mode;
	}

	public LEDMode getMode() {
		return mMode;
	}
}
