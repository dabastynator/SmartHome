package de.neo.smarthome;

import java.util.ArrayList;
import java.util.List;

import de.neo.remote.rmi.RMILogger.LogPriority;

public class RemoteLogger {

	public static List<RemoteLogListener> mListeners = new ArrayList<RemoteLogger.RemoteLogListener>();

	public static void info(String message)
	{
		performLog(LogPriority.INFORMATION, message, "");
	}
	
	public static void error(String message)
	{
		performLog(LogPriority.ERROR, message, "");
	}
	
	public static void warning(String message)
	{
		performLog(LogPriority.WARNING, message, "");
	}
	
	public static void performLog(LogPriority priority, String message,
			String object)
	{
		long time = System.currentTimeMillis();
		if (object == null)
			object = "";
		for (RemoteLogListener listener : mListeners)
			listener.remoteLog(priority, message, object, time);
	}

	public interface RemoteLogListener {

		public void remoteLog(LogPriority priority, String message,
				String object, long date);

	}

}
