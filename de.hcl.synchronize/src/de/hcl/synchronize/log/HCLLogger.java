package de.hcl.synchronize.log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hcl.synchronize.log.IHCLLogListener.HCLType;
import de.hcl.synchronize.log.IHCLLogListener.IHCLMessage;

public class HCLLogger {

	/**
	 * list of log listener
	 */
	private static List<IHCLLogListener> listeners = new ArrayList<IHCLLogListener>();

	/**
	 * add new log listener
	 * 
	 * @param listener
	 */
	public static void addListener(IHCLLogListener listener) {
		listeners.add(listener);
	}

	/**
	 * remove log listener
	 * 
	 * @param listener
	 */
	public static void removeListener(IHCLLogListener listener) {
		listeners.remove(listener);
	}

	public static void performLog(String message, HCLType type, Object author) {
		if (author == null)
			author = "unknown";
		IHCLMessage hclMessage = new IHCLLogListener.IHCLMessage(message, type,
				new Date(), author);
		for (IHCLLogListener listener : listeners)
			listener.hclLog(hclMessage);
	}

}
