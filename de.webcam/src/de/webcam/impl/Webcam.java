package de.webcam.impl;

import javax.swing.JFrame;

import com.smaxe.uv.media.core.VideoFrame;
import com.smaxe.uv.na.WebcamFactory;
import com.smaxe.uv.na.webcam.IWebcam;

/**
 * the webcam handles the webcam functionality. it creates the camera and gets
 * new frames.
 * 
 * @author sebastian
 */
public class Webcam extends AbstractWebcam {

	public void startCapture() throws Exception {
		final JFrame frame = new JFrame();

		final IWebcam webcam = WebcamFactory.getWebcams(frame, "jitsi").get(0);
		if (webcam == null)
			throw new Exception("no camera");

		webcam.open(new IWebcam.FrameFormat(320, 240), new WebcamListener());
		webcam.startCapture();

	}

	/**
	 * the listener gets new frames.
	 * 
	 * @author sebastian
	 */
	private class WebcamListener implements IWebcam.IListener {

		public void onVideoFrame(final VideoFrame frame) {
			System.out.println(frame.width + "*" + frame.height + " [1,1]="
					+ frame.rgb[0]);
			fireVideoFrame(frame.width, frame.height, frame.rgb);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
