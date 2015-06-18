package de.neo.remote.action;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.neo.remote.AbstractControlUnit;
import de.neo.remote.api.ICommandAction;
import de.neo.remote.mediaserver.ThumbnailHandler;
import de.neo.rmi.api.RMILogger;
import de.neo.rmi.api.RMILogger.LogPriority;
import de.neo.rmi.protokol.RemoteException;

public class CommandAction implements ICommandAction {

	private Process mProcess;
	private String mCommand;
	private String[] mParameter;
	private OutputListener mOutListener;
	private OutputListener mErrListener;
	private int[] mThumbnail;
	private int mWidth;
	private int mHeight;
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
			BufferedImage src = ImageIO.read(new File(thumbnail));
			// Create a buffered image with transparency
			mWidth = src.getWidth(null);
			mHeight = src.getHeight(null);
			BufferedImage bimage = new BufferedImage(mWidth, mHeight,
					BufferedImage.TYPE_INT_ARGB);

			// Draw the image on to the buffered image
			Graphics2D bGr = bimage.createGraphics();
			bGr.drawImage(src, 0, 0, null);
			bGr.dispose();
			int[] rgb = ((DataBufferInt) bimage.getData().getDataBuffer())
					.getData();
			mThumbnail = ThumbnailHandler.compressRGB565(rgb, mWidth, mHeight);
		}
		if (element.hasAttribute("logFile")) {
			mLogfile = new File(element.getAttribute("logFile"));
		}
	}

	@Override
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

	@Override
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

	@Override
	public int[] getThumbnail() {
		return mThumbnail;
	}

	@Override
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
				RMILogger.performLog(LogPriority.ERROR, "Cant create logfile: "
						+ e.getMessage(), "CommandAction");
			}
		}

		@Override
		public void run() {
			BufferedReader mReader = new BufferedReader(new InputStreamReader(
					mStream));
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
						mLogStream.write("ERROR: "
								+ e.getClass().getSimpleName() + ": "
								+ e.getMessage());
				} catch (IOException ignore) {
				}
			}
			mProcess = null;
		}
	}

	@Override
	public int getThumbnailWidth() throws RemoteException {
		return mWidth;
	}

	@Override
	public int getThumbnailHeight() throws RemoteException {
		return mHeight;
	}

	@Override
	public String getClientAction() throws RemoteException {
		return mClientAction;
	}

}
