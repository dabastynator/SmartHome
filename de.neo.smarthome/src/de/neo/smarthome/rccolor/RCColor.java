package de.neo.smarthome.rccolor;

import de.neo.smarthome.api.IWebLEDStrip.LEDMode;
import de.neo.smarthome.gpio.GPIOSender;

public class RCColor {

	private int mCurrentColor;

	public void setColor(int color) {
		mCurrentColor = color;
		GPIOSender.getInstance().setColor(mCurrentColor);
	}

	public void setColor(int color, int duration) {
		GPIOSender.getInstance().setColor(color);
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		GPIOSender.getInstance().setColor(mCurrentColor);
	}

	public int getColor() {
		return mCurrentColor;
	}

	public void setMode(LEDMode mode) {
		GPIOSender.getInstance().setMode(mode);
	}

}
