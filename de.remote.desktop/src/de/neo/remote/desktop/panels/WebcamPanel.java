package de.neo.remote.desktop.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.protokol.RemoteException;
import de.webcam.api.IWebcam;
import de.webcam.api.IWebcamListener;
import de.webcam.api.WebcamException;

/**
 * the webcam panel displays captured images by any webcam registred at the
 * Registry.
 * 
 * @author sebastian
 */
public class WebcamPanel extends Panel {

	/**
	 * generated id
	 */
	private static final long serialVersionUID = -4854699424009009921L;

	/**
	 * component for captured images
	 */
	private ViewComponent webcamPicture;

	/**
	 * captured picture
	 */
	private ViewComponent capturedPicture;

	/**
	 * remote webcam object
	 */
	private IWebcam webcam;

	/**
	 * allocate and initialize the webcam panel
	 */
	public WebcamPanel() {
		setName("Webcam");

		setLayout(new BorderLayout());
		add(BorderLayout.CENTER, createPictures());
		add(BorderLayout.NORTH, createButtons());
	}

	private Component createPictures() {
		webcamPicture = new ViewComponent();
		capturedPicture = new ViewComponent();
		Panel p = new Panel();
		p.setLayout(new GridLayout(1, 2));
		p.add(webcamPicture);
		p.add(capturedPicture);
		return p;
	}

	private Component createButtons() {
		JButton start = new JButton("start");
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					webcam.startCapture();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				} catch (WebcamException e1) {
					e1.printStackTrace();
				}
			}
		});
		JButton stop = new JButton("stop");
		stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					webcam.stopCapture();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				} catch (WebcamException e1) {
					e1.printStackTrace();
				}
			}
		});
		JButton take = new JButton("take picture");
		take.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int width = webcamPicture.image.getWidth();
				int height = webcamPicture.image.getHeight();
				int[] rgb = new int[width * height];
				webcamPicture.image.getRGB(0, 0, width, height, rgb, 0, width);
				capturedPicture.setImage(width, height, rgb);
				WebcamPanel.this.getParent().repaint();
			}
		});
		Panel p = new Panel();
		p.setLayout(new GridLayout(1, 3));
		p.add(start);
		p.add(stop);
		p.add(take);
		return p;
	}

	public void registerWebcamListener() {
		Server s = Server.getServer();
		if (!s.isConnectedToRegistry())
			return;

		try {
			webcam = (IWebcam) s.find(IWebcam.WEBCAM_SERVER, IWebcam.class);
			webcam.addWebcamListener(new WebListener());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * the listener gets new images and displays them on the image component
	 * 
	 * @author sebastian
	 */
	public class WebListener implements IWebcamListener {

		@Override
		public void onVideoFrame(final int width, final int height,
				final int[] rgb) throws RemoteException {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {

					webcamPicture.setImage(width, height, rgb);

					if (webcamPicture.getWidth() != width
							| webcamPicture.getHeight() != height)
						webcamPicture.setSize(width, height);
				}
			});
		}
	}

	/**
	 * the view component displays an image
	 * 
	 * @author sebastian
	 */
	private static class ViewComponent extends JComponent {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private BufferedImage image;

		public void setImage(int width, int height, int[] rgb) {
			this.image = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_ARGB);
			image.setRGB(0, 0, width, height, rgb, 0, width);
			if (image != null) {
				setBounds(new Rectangle(0, 0, image.getWidth(null),
						image.getHeight(null)));
				repaint();
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			if (image != null)
				g.drawImage(image, 0, 0, this);
		}
	}

}
