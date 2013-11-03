package de.neo.rmi.api;

import java.util.ArrayList;
import java.util.List;

/**
 * The logger gets log information and informs listener.
 * 
 * @author sebastian
 * 
 */
public class RMILogger {

	public enum LogPriority {
		INFORMATION, WARNING, ERROR
	};

	private static List<RMILogListener> listeners = new ArrayList<RMILogger.RMILogListener>();

	/**
	 * Perform log. All log listeners will be informed about the log.
	 * 
	 * @param priority
	 * @param message
	 * @param id
	 *            id of global object. Can be null, if no object is involved
	 */
	public static void performLog(LogPriority priority, String message,
			String id) {
		long time = System.currentTimeMillis();
		if (id == null)
			id = "";
		for (RMILogListener listener : listeners)
			listener.rmiLog(priority, message, id, time);
	}

	/**
	 * add new log listener
	 * 
	 * @param listener
	 */
	public static void addLogListener(RMILogListener listener) {
		listeners.add(listener);
	}

	/**
	 * remove log listener
	 * 
	 * @param listener
	 */
	public static void removeLogListener(RMILogListener listener) {
		listeners.remove(listener);
	}

	/**
	 * the log listener gets log information from the rmi logger.
	 * 
	 * @author sebastian
	 * 
	 */
	public interface RMILogListener {

		/**
		 * Inform about log with given priority, message and date when the log
		 * occurs. The id is the id of the involved global object, if there is
		 * any one.
		 * 
		 * @param priority
		 * @param message
		 * @param id
		 * @param date
		 */
		public void rmiLog(LogPriority priority, String message, String id,
				long date);

	}

}
