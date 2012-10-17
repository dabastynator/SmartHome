package de.hcl.synchronize.client;

import de.hcl.synchronize.log.IHCLLogListener;
import de.hcl.synchronize.util.SystemNotification;
import de.hcl.synchronize.util.SystemNotification.Urgency;

/**
 * the home cloud notificator gets logs and displays them on the screen.
 * 
 * @author sebastian
 */
public class HCLNotificator implements IHCLLogListener {

	/**
	 * All possible action types.
	 * 
	 * @author sebastian
	 */
	private enum Action {
		CREATE, SYNCH, DELETE, SEND, ERROR, INFORM
	};

	/**
	 * The current action.
	 */
	private Action currentAction;

	/**
	 * The count off the current action.
	 */
	private int actionTime;

	/**
	 * Time stamp of the last notification.
	 */
	private long lastNotify;

	/**
	 * Time of the last log.
	 */
	private long lastLog;

	/**
	 * notification object
	 */
	private SystemNotification notificator;

	/**
	 * last occurred message
	 */
	private String message;

	/**
	 * Name of the notifier
	 */
	private String notifierName;

	/**
	 * The base path for the icons
	 */
	private String iconPath;

	public HCLNotificator(String name, String iconPath) {
		this.notifierName = name;
		this.iconPath = iconPath;
		new NotificationThread().start();
	}

	private void createNotification() {
		if (actionTime == 0)
			return;
		String icon = iconPath;
		String message = actionTime + " files has been ";
		switch (currentAction) {
		case CREATE:
			icon = iconPath + HCLClient.CREATE_ICON;
			message += "created";
			break;
		case DELETE:
			icon = iconPath + HCLClient.DELETE_ICON;
			message += "deleted";
			break;
		case SEND:
			icon = iconPath + HCLClient.SYNCH_ICON;
			message += "sended";
			break;
		case SYNCH:
			icon = iconPath + HCLClient.SYNCH_ICON;
			message += "updated";
			break;
		case ERROR:
			icon = iconPath + HCLClient.ERROR_ICON;
			message = this.message;
			break;
		case INFORM:
			icon = iconPath + HCLClient.INFORM_ICON;
			message = this.message;
			break;
		}
		if (actionTime == 1)
			message = this.message;
		actionTime = 0;
		lastNotify = System.currentTimeMillis();
		notificator.showNotification(notifierName, message, Urgency.CRITICAL,
				icon);
	}

	@Override
	public void hclLog(IHCLMessage message) {
		this.message = message.message;
		switch (message.type) {
		case CREATE:
			if (actionTime == 0 || currentAction == Action.CREATE) {
				currentAction = Action.CREATE;
				actionTime++;
			} else if (currentAction == Action.SYNCH
					|| currentAction == Action.DELETE
					|| currentAction == Action.SEND) {
				currentAction = Action.SYNCH;
				actionTime++;
			}
			break;
		case DELETE:
			if (actionTime == 0 || currentAction == Action.DELETE) {
				currentAction = Action.DELETE;
				actionTime++;
			} else if (currentAction == Action.SYNCH
					|| currentAction == Action.CREATE
					|| currentAction == Action.SEND) {
				currentAction = Action.SYNCH;
				actionTime++;
			}
			break;
		case UPDATE:
			if (actionTime == 0 || currentAction == Action.SYNCH) {
				currentAction = Action.SYNCH;
				actionTime++;
			} else if (currentAction == Action.CREATE
					|| currentAction == Action.DELETE
					|| currentAction == Action.SEND) {
				currentAction = Action.SYNCH;
				actionTime++;
			}
			break;
		case SEND:
			if (actionTime == 0 || currentAction == Action.SYNCH) {
				currentAction = Action.SYNCH;
				actionTime++;
			} else if (currentAction == Action.CREATE
					|| currentAction == Action.DELETE
					|| currentAction == Action.SYNCH) {
				currentAction = Action.SYNCH;
				actionTime++;
			}
			break;
		case INFORMATION:
			if (actionTime == 0 || currentAction == Action.INFORM) {
				currentAction = Action.INFORM;
				actionTime++;
			}
			break;
		case WARNING:
		case ERROR:
			currentAction = Action.ERROR;
			actionTime++;
			break;
		}
		long now = System.currentTimeMillis();
		lastLog = now;
	}

	private class NotificationThread extends Thread {

		public static final long MINIMAL_NOTIFY_PAUSE = 10000;

		@Override
		public void run() {
			notificator = new SystemNotification();
			actionTime = 0;
			lastNotify = 0;
			lastLog = Long.MIN_VALUE;
			while (true) {
				boolean notify = checkForNotification();
				try {
					if (notify)
						Thread.sleep(MINIMAL_NOTIFY_PAUSE);
					else
						Thread.sleep(500);
				} catch (InterruptedException e) {
				}
			}

		}

		/**
		 * @return true, if notification was created.
		 */
		private boolean checkForNotification() {
			long now = System.currentTimeMillis();
			if (actionTime == 0 || now < lastNotify + MINIMAL_NOTIFY_PAUSE)
				return false;
			long waitForNext = 0;
			if (actionTime > 1)
				waitForNext = 7000;
			if ((now < lastLog + waitForNext)
					&& !(currentAction == Action.ERROR))
				return false;
			createNotification();
			return true;
		}
	}

}
