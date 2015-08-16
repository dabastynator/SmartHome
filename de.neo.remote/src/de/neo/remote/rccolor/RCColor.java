package de.neo.remote.rccolor;

import de.neo.remote.api.IRCColor;
import de.neo.remote.gpio.GPIOSender;

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

}
