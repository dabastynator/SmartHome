/**
 * Copyright (c) 2006 - 2012 Smaxe Ltd (www.smaxe.com).
 * All rights reserved.
 */

package de.webcam.test;

import com.smaxe.uv.media.VideoFrameFactory;
import com.smaxe.uv.media.core.VideoFrame;
import com.smaxe.uv.media.swing.JVideoScreen;
import com.smaxe.uv.na.WebcamFactory;
import com.smaxe.uv.na.webcam.IWebcam;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * <code>ExNativeAccessWebcam</code> - {@link IWebcam} usage example.
 * 
 * @author Andrei Sochirca
 */
public final class ExNativeAccessWebcam extends Object {
	/**
	 * Entry point.
	 * 
	 * @param args
	 * @throws Exception
	 *             if an exception occurred
	 */
	public static void main(final String[] args) throws Exception {
		final JComboBox webcamComboBox = new JComboBox();

		final JFrame frame = new JFrame();

		final JPanel content = new JPanel(new FlowLayout());

		content.setBorder(new EmptyBorder(8, 8, 8, 8));
		content.setPreferredSize(new Dimension(640, 48));

		content.add(new JLabel("Webcam: ", JLabel.RIGHT));
		content.add(webcamComboBox);
		content.add(new JButton(new AbstractAction("Open") {
			private final static long serialVersionUID = -4792981545160764997L;

			public void actionPerformed(ActionEvent e) {
				final IWebcam webcam = (IWebcam) webcamComboBox
						.getSelectedItem();
				if (webcam == null)
					return;

				final AtomicReference<JFrame> frameRef = new AtomicReference<JFrame>();

				final JVideoScreen videoScreen = new JVideoScreen();
				final AtomicBoolean videoScreenFlip = new AtomicBoolean(false);
				final AtomicBoolean videoScreenMirror = new AtomicBoolean(false);

				new Thread(new Runnable() {
					public void run() {
						final AtomicReference<VideoFrame> lastFrameRef = new AtomicReference<VideoFrame>();

						try {
							webcam.open(new IWebcam.FrameFormat(320, 240),
									new IWebcam.IListener() {
										private VideoFrame lastFrame = new VideoFrame(
												0, 0, null);

										public void onVideoFrame(
												final VideoFrame frame) {
											SwingUtilities
													.invokeLater(new Runnable() {
														public void run() {
															videoScreen
																	.setFrame(frame);

															if (lastFrame.width != frame.width
																	|| lastFrame.height != frame.height) {
																final JFrame frame = frameRef
																		.get();

																if (frame != null)
																	frame.pack();
															}

															lastFrame = frame;

															lastFrameRef
																	.set(lastFrame);
														}
													});
										}
									});

							webcam.startCapture();

							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									final JFrame frame = new JFrame();

									frameRef.set(frame);

									frame.getContentPane().setLayout(
											new BorderLayout());
									frame.getContentPane().add(videoScreen,
											BorderLayout.CENTER);

									frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
									frame.setResizable(false);
									frame.setTitle(webcam.getName());

									videoScreen
											.addMouseListener(new MouseAdapter() {
												@Override
												public void mouseClicked(
														final MouseEvent e) {
													final int clickCount = e
															.getClickCount();
													final Object source = e
															.getSource();

													switch (clickCount) {
													case 1: {
														if (SwingUtilities
																.isRightMouseButton(e)) {
															JPopupMenu popup = new JPopupMenu();

															popup.add(new AbstractAction(
																	"Take Snapshot") {
																private final static long serialVersionUID = 0L;

																public void actionPerformed(
																		ActionEvent e) {
																	new SnapshotDialog(
																			VideoFrameFactory
																					.clone(lastFrameRef
																							.get()))
																			.setVisible(true);
																}
															});

															popup.addSeparator();

															popup.add(new AbstractAction(
																	"Mirror") {
																private final static long serialVersionUID = 0L;

																public void actionPerformed(
																		ActionEvent e) {
																	videoScreenMirror
																			.set(!videoScreenMirror
																					.get());
																	videoScreen
																			.mirror(videoScreenMirror
																					.get());
																}
															});

															popup.add(new AbstractAction(
																	"Flip") {
																private final static long serialVersionUID = 0L;

																public void actionPerformed(
																		ActionEvent e) {
																	videoScreenFlip
																			.set(!videoScreenFlip
																					.get());
																	videoScreen
																			.flip(videoScreenFlip
																					.get());
																}
															});

															popup.addSeparator();

															popup.add(new AbstractAction(
																	"160x120") {
																private final static long serialVersionUID = 0L;

																public void actionPerformed(
																		ActionEvent e) {
																	webcam.setFrameFormat(new IWebcam.FrameFormat(
																			160,
																			120));
																}
															});

															popup.add(new AbstractAction(
																	"320x240") {
																private final static long serialVersionUID = 0L;

																public void actionPerformed(
																		ActionEvent e) {
																	webcam.setFrameFormat(new IWebcam.FrameFormat(
																			320,
																			240));
																}
															});

															popup.add(new AbstractAction(
																	"640x480") {
																private final static long serialVersionUID = 0L;

																public void actionPerformed(
																		ActionEvent e) {
																	webcam.setFrameFormat(new IWebcam.FrameFormat(
																			640,
																			480));
																}
															});

															popup.show(
																	(Component) source,
																	e.getX(),
																	e.getY());
														}
													}
														break;
													}
												}
											});

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
							JOptionPane.showMessageDialog(frame,
									ex.getMessage(), ex.getMessage(),
									JOptionPane.WARNING_MESSAGE);
						}
					}
				}).start();
			}
		}));

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(content);

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setTitle("Webcam capture options");

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		frame.pack();
		frame.setVisible(true);

		webcamComboBox.setModel(new WebcamComboModel(WebcamFactory.getWebcams(
				frame, args.length == 0 ? "jitsi" : args[0])));
	}

	/**
	 * <code>SnapshotDialog</code> - snapshot dialog.
	 */
	private final static class SnapshotDialog extends JDialog {
		private final static long serialVersionUID = -3136925779409522531L;

		/**
		 * Constructor.
		 * 
		 * @param videoFrame
		 */
		public SnapshotDialog(final VideoFrame videoFrame) {
			this.setLayout(new BorderLayout());
			this.setResizable(false);
			this.setTitle("Photo snapshot");

			JVideoScreen videoScreen = new JVideoScreen();

			videoScreen.setFrame(videoFrame);

			videoScreen.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(final MouseEvent e) {
					final int clickCount = e.getClickCount();
					final Object source = e.getSource();

					switch (clickCount) {
					case 1: {
						if (SwingUtilities.isRightMouseButton(e)) {
							JPopupMenu popup = new JPopupMenu();

							popup.add(new AbstractAction("Save As..") {
								private final static long serialVersionUID = 0L;

								public void actionPerformed(ActionEvent e) {
									JFileChooser fileChooser = new JFileChooser();

									fileChooser.setCurrentDirectory(new File(
											"."));

									final int rv = fileChooser
											.showSaveDialog(getParent());

									if (rv != JFileChooser.APPROVE_OPTION)
										return;

									final File file = fileChooser
											.getSelectedFile();

									new Thread(new Runnable() {
										public void run() {
											try {
												VideoFrameFactory.saveAsJpg(
														file, videoFrame);
											} catch (IOException e) {
												e.printStackTrace();
											}
										}
									}).start();
								}
							});

							popup.show((Component) source, e.getX(), e.getY());
						}
					}
						break;
					}
				}
			});

			this.add(videoScreen, BorderLayout.CENTER);
			this.pack();
		}
	}

	/**
	 * <code>WebcamComboModel</code> - webcam combo model.
	 */
	private final static class WebcamComboModel extends AbstractListModel
			implements ComboBoxModel {
		private final static long serialVersionUID = -8627944517955777531L;

		// fields
		private final List<IWebcam> devices;
		// state
		private Object selected = null;

		/**
		 * Constructor.
		 * 
		 * @param devices
		 */
		public WebcamComboModel(List<IWebcam> devices) {
			this.devices = devices;
		}

		// MutableComboBoxModel implementation

		public void setSelectedItem(final Object item) {
			this.selected = item;
		}

		public Object getSelectedItem() {
			return selected;
		}

		public Object getElementAt(int index) {
			return devices.get(index);
		}

		public int getSize() {
			return devices.size();
		}
	}
}