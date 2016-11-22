package de.neo.remote.gpio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.neo.remote.RemoteLogger;
import de.neo.rmi.api.RMILogger.LogPriority;

/**
 * The serial reader reads information from the arduino board. It expects codes
 * for switch actions. The first three lines must contain the regex to grep
 * famlycode, switchnumber and state.
 * 
 * @author sebastian
 */
public class SerialReader extends Thread {

	public static final long CHECK_INTERVALL_MIN = 1000 * 2;
	public static final long CHECK_INTERVALL_MAX = 1000 * 60 * 30;

	private static SerialReader mInstance;

	public static SerialReader getInstance() {
		if (mInstance == null)
			mInstance = new SerialReader();
		return mInstance;
	}

	private List<ISerialListener> mListener = new ArrayList<ISerialListener>();
	private SerialReadThread mSerialReader;
	private long mCheckDuration = CHECK_INTERVALL_MIN;

	public SerialReader() {
		// start();
	}

	@Override
	public void run() {
		while (true) {
			// TODO
		}
	}

	private void informListener(String familyCode, int switchNumber, de.neo.remote.api.IWebSwitch.State state) {
		for (ISerialListener listener : mListener)
			listener.OnSwitchEvent(familyCode, switchNumber, state);
	}

	public List<ISerialListener> getListener() {
		return mListener;
	}

	private class SerialReadThread extends Thread {

		private BufferedReader mInput;
		private Pattern mPattern;

		public SerialReadThread(InputStream input) {
			mInput = new BufferedReader(new InputStreamReader(input));
		}

		@Override
		public void run() {
			try {
				initialize();
				String line = null;
				while ((line = mInput.readLine()) != null) {
					Matcher matcher = mPattern.matcher(line);
					if (!matcher.matches()) {
						RemoteLogger.performLog(LogPriority.WARNING, "No match for line: " + line, "SerialReader");
					} else {
						String familyCode = matcher.group(1);
						int switchNumber = Integer.parseInt(matcher.group(2));
						de.neo.remote.api.IWebSwitch.State switchState = (matcher.group(3).equals("1")
								|| matcher.group(3).equals("ON")) ? de.neo.remote.api.IWebSwitch.State.ON
										: de.neo.remote.api.IWebSwitch.State.OFF;
						SerialReader.getInstance().informListener(familyCode, switchNumber, switchState);
						RemoteLogger.performLog(LogPriority.INFORMATION, "Switch change family: " + familyCode
								+ " switch: " + switchNumber + " state: " + switchState, "SerialReader");
					}
				}
			} catch (IOException e) {
				RemoteLogger.performLog(LogPriority.ERROR,
						"Listening failed: " + e.getClass().getSimpleName() + ": " + e.getMessage(), "SerialReader");
			} finally {
				try {
					mInput.close();
				} catch (IOException e) {
				}
				SerialReader.getInstance().mSerialReader = null;
			}
		}

		private void initialize() throws IOException {
			String line = mInput.readLine();
			mPattern = Pattern.compile(line);
			RemoteLogger.performLog(LogPriority.INFORMATION, "Regex for switch lines: " + line, "SerialReader");
		}
	}

	public interface ISerialListener {

		public void OnSwitchEvent(String family, int switchNumber, de.neo.remote.api.IWebSwitch.State state);

	}
}
