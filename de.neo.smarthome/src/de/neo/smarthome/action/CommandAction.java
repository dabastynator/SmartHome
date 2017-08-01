package de.neo.smarthome.action;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.neo.remote.rmi.RMILogger;
import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.AbstractControlUnit;
import de.neo.smarthome.RemoteLogger;

public class CommandAction {

	private Process mProcess;
	private String mCommand;
	private String[] mParameter;
	private OutputListener mOutListener;
	private OutputListener mErrListener;
	private String mIconBase64;
	private String mClientAction = "";
	private File mLogfile;
	private AbstractControlUnit mUnit;

	public CommandAction(AbstractControlUnit unit) {
		mUnit = unit;
	}

	public void initialize(Element element) throws SAXException, IOException {
		if (!element.hasAttribute("command"))
			throw new SAXException("command missing for internet switch");
		mCommand = element.getAttribute("command");
		if (element.hasAttribute("parameter"))
			mParameter = element.getAttribute("parameter").split(" ");
		if (element.hasAttribute("clientAction"))
			mClientAction = element.getAttribute("clientAction");
		if (element.hasAttribute("thumbnail")) {
			String thumbnail = element.getAttribute("thumbnail");
			try {
				byte[] bytes = loadFile(new File(thumbnail));
				mIconBase64 = DatatypeConverter.printBase64Binary(bytes);
			} catch (IOException e) {
				try {
					RemoteLogger.performLog(LogPriority.ERROR, e.getClass().getSimpleName() + ": " + e.getMessage(),
							mUnit.getID());
				} catch (RemoteException e1) {
					// ignore
				}
			}
		}
		if (element.hasAttribute("logFile")) {
			mLogfile = new File(element.getAttribute("logFile"));
		}
	}

	private static byte[] loadFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		long length = file.length();
		if (length > Integer.MAX_VALUE) {
			is.close();
			throw new IOException("Thumbnail image is too large");
		}
		byte[] bytes = new byte[(int) length];

		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}
		is.close();
		if (offset < bytes.length)
			throw new IOException("Could not completely read file " + file.getName());

		return bytes;
	}

	public void startAction() throws IOException {
		if (isRunning()) {
			throw new IOException("Process is already running");
		} else {
			if (mParameter != null) {
				String[] execute = new String[mParameter.length + 1];
				execute[0] = mCommand;
				for (int i = 0; i < mParameter.length; i++)
					execute[i + 1] = mParameter[i];
				mProcess = Runtime.getRuntime().exec(execute);
			} else
				mProcess = Runtime.getRuntime().exec(mCommand);
			mOutListener = new OutputListener(mProcess.getInputStream());
			mOutListener.start();
			mErrListener = new OutputListener(mProcess.getErrorStream());
			mErrListener.start();
			Map<String, String> parameterExchange = new HashMap<String, String>();
			parameterExchange.put("@action", "start");
			mUnit.fireTrigger(parameterExchange, "@action=start");
		}
	}

	public void stopAction() {
		mProcess.destroy();
		mProcess = null;
		if (mOutListener != null)
			mOutListener.mRunning = false;
		if (mErrListener != null)
			mErrListener.mRunning = false;
		mOutListener = null;
		mErrListener = null;
		Map<String, String> parameterExchange = new HashMap<String, String>();
		parameterExchange.put("@action", "stop");
		mUnit.fireTrigger(parameterExchange, "@action=stop");
	}

	public String getIconBase64() throws RemoteException {
		return mIconBase64;
	}

	public boolean isRunning() {
		if (mProcess == null)
			return false;
		try {
			mProcess.exitValue();
			mProcess = null;
			mOutListener.mRunning = false;
			mOutListener = null;
			mErrListener.mRunning = false;
			mErrListener = null;
			return false;
		} catch (Exception e) {
			return true;
		}
	}

	class OutputListener extends Thread {

		private InputStream mStream;
		private boolean mRunning;
		private BufferedWriter mLogStream;

		public OutputListener(InputStream inputStream) {
			mStream = inputStream;
			try {
				if (mLogfile != null)
					mLogStream = new BufferedWriter(new FileWriter(mLogfile));
			} catch (IOException e) {
				RMILogger.performLog(LogPriority.ERROR, "Cant create logfile: " + e.getMessage(), "CommandAction");
			}
		}

		@Override
		public void run() {
			BufferedReader mReader = new BufferedReader(new InputStreamReader(mStream));
			mRunning = true;
			String line = null;
			try {
				while ((line = mReader.readLine()) != null && mRunning) {
					if (mLogStream != null) {
						mLogStream.write(line);
						mLogStream.flush();
					}
				}
			} catch (IOException e) {
				try {
					if (mLogStream != null)
						mLogStream.write("ERROR: " + e.getClass().getSimpleName() + ": " + e.getMessage());
				} catch (IOException ignore) {
				}
			}
			mProcess = null;
		}
	}

	public String getClientAction() throws RemoteException {
		return mClientAction;
	}

}
