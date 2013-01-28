package de.remote.impl;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.IOException;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.ControlConstants;
import de.remote.api.IControl;

public class ControlImpl implements IControl {

	public static final String MAKE_DARK = "xset dpms force off";
	public static final String MAKE_BRIDE = "xset dpms force on";
	public static final String EXIT_SCREENSAVER = "gnome-screensaver-command --exit";
	public static final String SHUTDOWN = "shutdown -h now";

	private Robot robot;

	@Override
	public void shutdown() {
		try {
			Runtime.getRuntime().exec(SHUTDOWN);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void displayDark() {
		try {
			Runtime.getRuntime().exec(MAKE_DARK);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void displayBride() {
		try {
			Runtime.getRuntime().exec(MAKE_BRIDE);
			Runtime.getRuntime().exec(EXIT_SCREENSAVER);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Robot getRobot() throws RemoteException {
		if (robot == null)
			try {
				robot = new Robot();
			} catch (AWTException e) {
				throw new RemoteException(ControlConstants.STATION_ID,
						"robot not available: " + e.getMessage());
			}
		return robot;
	}

	@Override
	public void mouseMove(int x, int y) throws RemoteException {
		getRobot().mouseMove(x, y);
	}

	@Override
	public void mousePress(int button) throws RemoteException {
		getRobot().mousePress(button);
	}

	@Override
	public void keyPress(int keycode) throws RemoteException {
		getRobot().keyPress(keycode);
	}

}
