package de.webcam.test;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.protokol.RemoteException;
import de.webcam.api.IWebcam;
import de.webcam.api.IWebcamListener;
import de.webcam.server.WebcamServer;

public class Client {

	public static void main(String[] args) throws Exception {

		Server s = Server.getServer();

		s.connectToRegistry("localhost");
		s.startServer(IWebcam.PORT + 1);
		IWebcam webcam = (IWebcam) s.find(IWebcam.WEBCAM_SERVER,
				IWebcam.class);

		webcam.addWebcamListener(new WebListener());

	}

	public static class WebListener implements IWebcamListener {

		private JFrame frame;
		private ViewComponent image;

		public WebListener() {
			frame = new JFrame();
			image = new ViewComponent();
			frame.add(image);
			frame.pack();
			frame.setVisible(true);
		}

		@Override
		public void onVideoFrame(final int width, final int height,
				final int[] rgb) throws RemoteException {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {

					System.out.println("rgb[0] = "
							+ Integer.toHexString(rgb[0]));
					image.setImage(width, height, rgb);

					if (frame.getWidth() != width | frame.getHeight() != height)
						frame.setSize(width, height);
				}
			});
		}
	}

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
