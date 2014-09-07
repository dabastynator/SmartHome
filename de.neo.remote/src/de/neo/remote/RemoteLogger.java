package de.neo.remote;

import java.util.ArrayList;
import java.util.List;

import de.neo.rmi.api.RMILogger.LogPriority;

/**
 * The logger gets log information and informs listener.
 * 
 * @author sebastian
 * 
 */
public class RemoteLogger {

	public static List<RemoteLogListener> listeners = new ArrayList<RemoteLogger.RemoteLogListener>();

	/**
	 * Perform log. All log listeners will be informed about the log.
	 * 
	 * @param priority
	 * @param message
	 * @param object
	 */
	public static void performLog(LogPriority priority, String message,
			String object) {
		long time = System.currentTimeMillis();
		if (object == null)
			object = "";
		for (RemoteLogListener listener : listeners)
			listener.remoteLog(priority, message, object, time);
	}

	/**
	 * the log listener gets log information from the rmi logger.
	 * 
	 * @author sebastian
	 * 
	 */
	public interface RemoteLogListener {

		/**
		 * Inform about log with given priority, message and date when the log
		 * occurs. The object is the name of the involved global object, if
		 * there is any one.
		 * 
		 * @param priority
		 * @param message
		 * @param object
		 * @param date
		 */
		public void remoteLog(LogPriority priority, String message,
				String object, long date);

	}

}
