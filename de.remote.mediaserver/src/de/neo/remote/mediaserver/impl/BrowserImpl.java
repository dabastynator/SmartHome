package de.neo.remote.mediaserver.impl;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import de.neo.remote.mediaserver.api.IBrowser;
import de.neo.remote.mediaserver.api.IThumbnailListener;
import de.newsystem.rmi.api.Oneway;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.protokol.RemoteException;
import de.newsystem.rmi.transceiver.DirectorySender;
import de.newsystem.rmi.transceiver.FileReceiver;
import de.newsystem.rmi.transceiver.FileSender;

public class BrowserImpl implements IBrowser {

	private String location;
	private String root;

	/**
	 * Create new browser
	 * @param path to root directory for the browser
	 */
	public BrowserImpl(String string) {
		if (!string.endsWith(File.separator))
			string += File.separator;
		root = location = string;
	}

	@Override
	public boolean goBack() {
		if (root.equals(location))
			return false;
		location = location.substring(0, location.lastIndexOf(File.separator));
		location = location.substring(0,
				location.lastIndexOf(File.separator) + 1);
		return true;
	}

	@Override
	public void goTo(String directory) {
		if (!location.endsWith(File.separator))
			location += File.separator;
		location += directory + File.separator;
	}

	@Override
	public String[] getDirectories() {
		List<String> list = new ArrayList<String>();
		for (String str : new File(location).list())
			if (new File(location + str).isDirectory())
				if (str.length() > 0 && str.charAt(0) != '.')
					list.add(str);
		return list.toArray(new String[] {});
	}

	@Override
	public String[] getFiles() {
		List<String> list = new ArrayList<String>();
		for (String str : new File(location).list())
			if (new File(location + str).isFile())
				if (str.length() > 0 && str.charAt(0) != '.')
					list.add(str);
		return list.toArray(new String[] {});
	}

	@Override
	public String getLocation() {
		if (location.lastIndexOf(File.separator) >= 0) {
			String str = location.substring(0,
					location.lastIndexOf(File.separator));
			return str.substring(str.lastIndexOf(File.separator) + 1);
		}
		return location;
	}

	@Override
	public String getFullLocation() {
		return location;
	}

	@Override
	public boolean delete(String file) throws RemoteException {
		return new File(file).delete();
	}

	@Override
	public String publishFile(String file, int port) throws RemoteException,
			IOException {
		FileSender sender = new FileSender(new File(location + file), port, 1);
		sender.sendAsync();
		return Server.getServer().getServerPort().getIp();
	}

	@Override
	public String publishDirectory(String directory, int port)
			throws RemoteException, IOException {
		DirectorySender sender = new DirectorySender(new File(location
				+ directory), port, 1);
		sender.sendAsync();
		return Server.getServer().getServerPort().getIp();
	}

	@Override
	public void updloadFile(String file, String serverIp, int port)
			throws RemoteException {
		FileReceiver receiver = new FileReceiver(serverIp, port, new File(
				location + file));
		receiver.receiveAsync();
	}

	private BufferedImage scale(BufferedImage source, int width, int high) {
		int w = width;
		int h = high;
		BufferedImage bi = getCompatibleImage(w, h);
		Graphics2D g2d = bi.createGraphics();
		double xScale = (double) w / source.getWidth();
		double yScale = (double) h / source.getHeight();
		AffineTransform at = AffineTransform.getScaleInstance(xScale, yScale);
		g2d.drawRenderedImage(source, at);
		g2d.dispose();
		return bi;
	}

	private BufferedImage getCompatibleImage(int w, int h) {
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		BufferedImage image = gc.createCompatibleImage(w, h);
		return image;
	}

	@Override
	public void fireThumbnails(IThumbnailListener listener, int width,
			int height) throws RemoteException {
		for (String fileName : new File(location).list()) {
			File file = new File(location + fileName);
			if (file.isFile() && fileName.length() > 3) {
				String extension = fileName.toUpperCase().substring(
						fileName.length() - 3);
				if (extension.equals("JPG") || extension.equals("PNG")
						|| extension.equals("GIF")) {
					try {
						BufferedImage src = ImageIO.read(file);
						double radio = Math.min(
								width / (double) src.getWidth(), height
										/ (double) src.getHeight());
						int w = (int) (radio * src.getWidth());
						int h = (int) (radio * src.getHeight());
						if (w%2!=0)
							w++;
						BufferedImage thumbnail = scale(src, w, h);
						int[] rgb = ((DataBufferInt) thumbnail.getData()
								.getDataBuffer()).getData();
						rgb = compressRGB565(rgb, w, h);
						System.out.println("send thumbnail: " + fileName);
						listener.setThumbnail(fileName, w, h, rgb);
					} catch (Exception e) {
						file.getName();
					}
				}
			}
		}
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

	private int[] compressRGB565(int[] rgb, int width, int height) {
		int[] ret = new int[width * height / 2];
		for (int j = 0; j < height; j++)
			for (int i = 0; i < width; i += 2) {
				int px1 = compressRGB565(rgb[j * width + i]);
				int px2 = compressRGB565(rgb[j * width + i + 1]);
				ret[j * width / 2 + i / 2] = px1 | (px2 << 16);
			}
		return ret;
	}

	public static void main(String[] args) {
		try {
			BrowserImpl impl = new BrowserImpl("/home/sebastian/temp");
			impl.fireThumbnails(new IThumbnailListener() {
				@Override
				@Oneway
				public void setThumbnail(String file, int w, int h,
						int[] thumbnail) throws RemoteException {
					System.out.println(thumbnail.length);
				}
			}, 10, 10);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
