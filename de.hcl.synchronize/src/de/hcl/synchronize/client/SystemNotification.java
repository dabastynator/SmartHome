package de.hcl.synchronize.client;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The system notification creates notifications on the os.
 * 
 * @author sebastian
 */
public class SystemNotification {

	/**
	 * The Urgency defines the level of the notification
	 * 
	 * @author sebastian
	 */
	public enum Urgency {
		LOW, NORMAL, CRITICAL
	};

	/**
	 * The notify technic defines the way to notify on the system.
	 * 
	 * @author sebastian
	 */
	private enum NotifyTechnic {
		NotifyOSD, TrayIcon
	};

	/**
	 * current technic for this system
	 */
	private NotifyTechnic technic;

	/**
	 * The tray if TrayIcon is the technic
	 */
	private SystemTray tray;

	/**
	 * Allocate new notification object. Show notification can be called
	 * Multiple.
	 */
	public SystemNotification() {
		String osName = System.getProperty("os.name");
		if (osName.toLowerCase().startsWith("linux"))
			technic = NotifyTechnic.NotifyOSD;
		else
			technic = NotifyTechnic.TrayIcon;
		if (technic == NotifyTechnic.TrayIcon) {
			tray = SystemTray.getSystemTray();
		}
	}

	/**
	 * Show new notification with given header, message and level of the
	 * notification.
	 * 
	 * @param header
	 * @param message
	 * @param urgency
	 */
	public void showNotification(String header, String message, Urgency urgency) {
		showNotification(header, message, urgency, "");
	}

	/**
	 * Show new notification with given header, message, icon and level of the
	 * notification.
	 * 
	 * @param header
	 * @param message
	 * @param urgency
	 * @param icon
	 */
	public void showNotification(String header, String message,
			Urgency urgency, String icon) {
		switch (technic) {
		case NotifyOSD:
			showNotificationNotifyOSD(header, message, urgency, icon);
			break;
		case TrayIcon:
			showNotificationTrayIcon(header, message, urgency, icon);
			break;
		}
	}

	/**
	 * NotifyOSD specific notification.
	 * 
	 * @param header
	 * @param message
	 * @param urgency
	 * @param icon
	 */
	private void showNotificationNotifyOSD(String header, String message,
			Urgency urgency, String icon) {
		List<String> cmd = new ArrayList<String>();
		cmd.add("notify-send");
		cmd.add("-u");
		cmd.add(urgency.toString().toLowerCase());
		if (icon.length() > 0) {
			cmd.add("-i");
			cmd.add(icon);
		}
		cmd.add(header);
		cmd.add(message);
		try {
			Runtime.getRuntime().exec(cmd.toArray(new String[] {}));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * TrayIcon specific notification.
	 * 
	 * @param header
	 * @param message
	 * @param urgency
	 * @param icon
	 */
	private void showNotificationTrayIcon(String header, String message,
			Urgency urgency, String icon) {
		TrayIcon trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(icon), header);
		MessageType type = MessageType.NONE;
		switch (urgency) {
		case CRITICAL:
			type = MessageType.ERROR;
			break;
		case NORMAL:
			type = MessageType.NONE;
		case LOW:
			type = MessageType.INFO;
		}
		try {
			tray.add(trayIcon);
			trayIcon.displayMessage(header, message, type);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new SystemNotification().showNotification("hey how", "na mal schaun",
				Urgency.CRITICAL,
				"/home/sebastian/Downloads/synch.png");
	}
}
