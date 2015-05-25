package de.neo.remote.action;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.neo.remote.api.ICommandAction;
import de.neo.remote.mediaserver.ThumbnailHandler;
import de.neo.rmi.api.RMILogger;
import de.neo.rmi.api.RMILogger.LogPriority;
import de.neo.rmi.protokol.RemoteException;

public class CommandAction implements ICommandAction {

	private Process mProcess;
	private String mCommand;
	private String[] mParameter;
	private OutputListener mListener;
	private int[] mThumbnail;
	private int mWidth;
	private int mHeight;
	private String mClientAction = "";

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
	}

	@Override
	public void startAction() throws IOException {
		if (isRunning()) {
			throw new IOException("Process is already running");
		} else {
			mProcess = Runtime.getRuntime().exec(mCommand, mParameter);
			mListener = new OutputListener(mProcess.getInputStream());
		}
	}

	@Override
	public void stopAction() {
		mProcess.destroy();
		mProcess = null;
		if (mListener != null)
			mListener.mRunning = false;
		mListener = null;
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
			mListener.mRunning = false;
			mListener = null;
			return false;
		} catch (Exception e) {
			return true;
		}
	}

	class OutputListener extends Thread {

		private InputStream mStream;
		private boolean mRunning;

		public OutputListener(InputStream inputStream) {
			mStream = inputStream;
		}

		@Override
		public void run() {
			BufferedReader mReader = new BufferedReader(new InputStreamReader(
					mStream));
			mRunning = true;
			String line = null;
			try {
				while ((line = mReader.readLine()) != null && mRunning) {
					RMILogger.performLog(LogPriority.INFORMATION, line,
							"CommandAction");
				}
			} catch (IOException e) {
				RMILogger.performLog(LogPriority.INFORMATION, e.getMessage(),
						"CommandAction");
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
