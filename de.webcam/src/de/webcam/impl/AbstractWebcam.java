package de.webcam.impl;

import java.util.HashMap;
import java.util.Map;

import de.newsystem.rmi.protokol.RemoteException;
import de.webcam.api.IWebcam;
import de.webcam.api.IWebcamListener;

/**
 * the abstract webcam handles basic observer functionality and fires new frame
 * to observer.
 * 
 * @author sebastian
 */
public abstract class AbstractWebcam implements IWebcam {

	/**
	 * list of all observer
	 */
	protected Map<IWebcamListener, ClientInformer> listeners = new HashMap<IWebcamListener, ClientInformer>();

	protected boolean capturing = false;

	private int width;
	private int height;
	private int[] rgb;

	@Override
	public void addWebcamListener(IWebcamListener listener)
			throws RemoteException {
		ClientInformer informer = new ClientInformer(listener);
		informer.start();
		listeners.put(listener, informer);
	}

	@Override
	public void addWebcamListener(IWebcamListener listener, int width,
			int height, int quality) throws RemoteException {
		if (quality == IWebcam.RGB_565) {
			width -= width % 2;
			height -= height % 2;
		}
		ClientInformer informer = new ClientInformer(listener, width, height,
				quality);
		informer.start();
		listeners.put(listener, informer);
	}

	@Override
	public void addWebcamListener(IWebcamListener listener, int width,
			int height) throws RemoteException {
		ClientInformer informer = new ClientInformer(listener, width, height,
				IWebcam.RGB_8888);
		informer.start();
		listeners.put(listener, informer);
	}

	@Override
	public void removeWebcamListener(IWebcamListener listener)
			throws RemoteException {
		ClientInformer informer = listeners.get(listener);
		if (informer != null)
			informer.stopInforming();
		listeners.remove(listener);
	}

	/**
	 * fire new frame to observer.
	 * 
	 * @param width
	 * @param height
	 * @param rgb
	 */
	protected void fireVideoFrame(int width, int height, int[] rgb) {
		this.width = width;
		this.height = height;
		this.rgb = rgb;
		for (ClientInformer informer : listeners.values())
			informer.newFrame();
	}

	/**
	 * the client informer sends the newest frame to the client in a own thread
	 * 
	 * @author sebastian
	 */
	private class ClientInformer extends Thread {

		private boolean informing = true;
		private IWebcamListener listener;
		private boolean dirty = true;
		private boolean compress = false;
		private int cWidth;
		private int cHeight;
		private int[] cRgb;
		private int quality;

		public ClientInformer(IWebcamListener listener) {
			this.listener = listener;
		}

		public ClientInformer(IWebcamListener listener, int width, int height,
				int quality) {
			this(listener);
			compress = true;
			cWidth = width;
			cHeight = height;
			this.quality = quality;
		}

		public void newFrame() {
			dirty = true;
		}

		public void stopInforming() {
			informing = false;
		}

		public int[] compress(int[] rgb) {
			if (quality == IWebcam.RGB_8888)
				return compressRGB8888(rgb);
			if (quality == IWebcam.RGB_565)
				return compressRGB565(rgb);
			return null;
		}

		private int[] compressRGB565(int[] rgb) {
			int[] ret = new int[cWidth * cHeight / 2];
			for (int j = 0; j < cHeight; j++)
				for (int i = 0; i < cWidth; i += 2) {
					int h = (int) ((((double) j) / cHeight) * height);
					int w1 = (int) ((((double) i) / cWidth) * width);
					int w2 = (int) ((((double) i + 1) / cWidth) * width);
					int px1 = compressRGB565(rgb[h * width + w1]);
					int px2 = compressRGB565(rgb[h * width + w2]);
					ret[j * cWidth / 2 + i / 2] = px1 | (px2 << 16);
				}
			return ret;
		}

		private int compressRGB565(int i) {
			int ret = 0;
			// blue 5 bit
			ret |= (0x000000F8 & i) >> 3;
			// green 6 bit
			ret |= (0x0000FC00 & i) >> 2 + 3;
			// red 5 bit
			ret |= (0x00F80000 & i) >> 3 + 2 + 3;
			return ret;
		}

		private int[] compressRGB8888(int[] rgb) {
			int[] ret = new int[cWidth * cHeight];
			for (int j = 0; j < cHeight; j++)
				for (int i = 0; i < cWidth; i++) {
					int h = (int) ((((double) j) / cHeight) * height);
					int w = (int) ((((double) i) / cWidth) * width);
					ret[j * cWidth + i] = rgb[h * width + w];
				}
			return ret;
		}

		@Override
		public void run() {
			while (informing) {
				if (dirty && rgb != null) {
					dirty = false;
					try {
						if (compress) {
							cRgb = compress(rgb);
							listener.onVideoFrame(cWidth, cHeight, cRgb);
						} else
							listener.onVideoFrame(width, height, rgb);
					} catch (RemoteException e1) {
						return;
					}
				} else {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

	}
}
