package de.hcl.synchronize.log;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hcl.synchronize.api.IHCLClient;
import de.hcl.synchronize.log.IHCLLog.HCLType;
import de.hcl.synchronize.log.IHCLLog.IHCLMessage;

public class HCLLogger {

	/**
	 * list of log listener
	 */
	private static List<IHCLLog> listeners = new ArrayList<IHCLLog>();

	/**
	 * add new log listener
	 * 
	 * @param listener
	 */
	public static void addListener(IHCLLog listener) {
		listeners.add(listener);
	}

	/**
	 * remove log listener
	 * 
	 * @param listener
	 */
	public static void removeListener(IHCLLog listener) {
		listeners.remove(listener);
	}

	public static void performLog(String message, HCLType type,
			IHCLClient author) {
		IHCLMessage hclMessage = new IHCLLog.IHCLMessage(message, type,
				new Date(), author);
		try {
			System.out.println(hclMessage.type.toString() + ": "
					+ hclMessage.message + " (" + author.getName() + "/"
					+ hclMessage.time.toString() + ")");
		} catch (RemoteException e) {
		}
		for (IHCLLog listener : listeners)
			listener.hclLog(hclMessage);
	}

}
