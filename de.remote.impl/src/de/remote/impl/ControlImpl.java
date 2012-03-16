package de.remote.impl;

import java.io.IOException;

import de.remote.api.IControl;

public class ControlImpl implements IControl {

	public static final String MAKE_DARK = "xset dpms force off";
	public static final String MAKE_BRIDE = "xset dpms force on";
	public static final String EXIT_SCREENSAVER = "gnome-screensaver-command --exit";
	public static final String SHUTDOWN = "shutdown -h now";

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

}
