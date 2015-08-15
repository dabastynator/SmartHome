package de.neo.remote.rccolor;

import de.neo.remote.api.IRCColor;
import de.neo.remote.gpio.GPIOSender;
import de.neo.rmi.protokol.RemoteException;

public class RCColor implements IRCColor {

	private int mCurrentColor;

	@Override
	public void setColor(int color) throws RemoteException {
		mCurrentColor = color;
		GPIOSender.getInstance().setColor(mCurrentColor);
	}

	@Override
	public void setColor(int color, int duration) throws RemoteException {
		GPIOSender.getInstance().setColor(color);
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		GPIOSender.getInstance().setColor(mCurrentColor);
	}

}
