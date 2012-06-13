package de.webcam.server;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.smaxe.uv.media.core.VideoFrame;
import com.smaxe.uv.media.swing.JVideoScreen;
import com.smaxe.uv.na.WebcamFactory;
import com.smaxe.uv.na.webcam.IWebcam;

public class SimpleAccess {
	/**
	 * Entry point.
	 * 
	 * @param args
	 * @throws Exception
	 *             if an exception occurred
	 */
	public static void main(final String[] args) throws Exception {
		final JFrame frame = new JFrame();

		final IWebcam webcam = WebcamFactory.getWebcams(frame,
				args.length == 0 ? "jitsi" : args[0]).get(0);
		if (webcam == null)
			throw new Exception("no camera");

		final AtomicReference<JFrame> frameRef = new AtomicReference<JFrame>();

		final JVideoScreen videoScreen = new JVideoScreen();

		new Thread(new Runnable() {
			public void run() {
				final AtomicReference<VideoFrame> lastFrameRef = new AtomicReference<VideoFrame>();

				try {
					webcam.open(new IWebcam.FrameFormat(320, 240),
							new IWebcam.IListener() {
								private VideoFrame lastFrame = new VideoFrame(
										0, 0, null);

								public void onVideoFrame(final VideoFrame frame) {
									SwingUtilities.invokeLater(new Runnable() {
										public void run() {
											videoScreen.setFrame(frame);

											System.out.println(frame.width
													+ "*" + frame.height
													+ " = " + frame.rgb.length);
											
											System.out.println("rgb[0] = " + Integer.toHexString(frame.rgb[0]));

											if (lastFrame.width != frame.width
													|| lastFrame.height != frame.height) {
												final JFrame frame = frameRef
														.get();

												if (frame != null)
													frame.pack();
											}

											lastFrame = frame;

											lastFrameRef.set(lastFrame);
										}
									});
								}
							});

					webcam.startCapture();

					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							final JFrame frame = new JFrame();

							frameRef.set(frame);

							frame.getContentPane()
									.setLayout(new BorderLayout());
							frame.getContentPane().add(videoScreen,
									BorderLayout.CENTER);

							frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
							frame.setResizable(false);
							frame.setTitle(webcam.getName());

							frame.addWindowListener(new WindowAdapter() {
								@Override
								public void windowClosing(WindowEvent e) {
									webcam.close();
								}
							});

							frame.pack();
							frame.setVisible(true);
						}
					});
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(frame, ex.getMessage(),
							ex.getMessage(), JOptionPane.WARNING_MESSAGE);
				}
			}
		}).start();

	}
}
