package de.neo.smarthome.rccolor;

import de.neo.smarthome.api.IRCColor;
import de.neo.smarthome.api.IWebLEDStrip.LEDMode;
import de.neo.smarthome.gpio.GPIOSender;

public class RCColor implements IRCColor {

	private int mCurrentColor;

	@Override
	public void setColor(int color) {
		mCurrentColor = color;
		GPIOSender.getInstance().setColor(mCurrentColor);
	}

	@Override
	public void setColor(int color, int duration) {
		GPIOSender.getInstance().setColor(color);
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		GPIOSender.getInstance().setColor(mCurrentColor);
	}

	@Override
	public int getColor() {
		return mCurrentColor;
	}

	@Override
	public void setMode(LEDMode mode) {
		GPIOSender.getInstance().setMode(mode);		
	}

}
