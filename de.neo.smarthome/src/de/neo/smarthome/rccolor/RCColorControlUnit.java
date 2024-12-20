package de.neo.smarthome.rccolor;

import de.neo.persist.annotations.Domain;
import de.neo.smarthome.AbstractControlUnit;
import de.neo.smarthome.api.IWebLEDStrip.LEDMode;
import de.neo.smarthome.switches.GPIOSender;

@Domain(name = "ColorSetter")
public class RCColorControlUnit extends AbstractControlUnit {

	private int mCurrentColor;

	private LEDMode mMode;

	private GPIOSender mSender = GPIOSender.getInstance();

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
