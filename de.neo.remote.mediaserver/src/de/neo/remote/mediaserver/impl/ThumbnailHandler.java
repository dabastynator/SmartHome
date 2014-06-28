package de.neo.remote.mediaserver.impl;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

public class ThumbnailHandler {

	public static final String THUMBNAILS = ".thumbnail";

	private static ThumbnailHandler handler;

	public static ThumbnailHandler instance() {
		return handler;
	}

	public static void init(String temporaryFolder) {
		handler = new ThumbnailHandler(temporaryFolder);
	}

	private File thumbnails;
	private List<ThumbnailListener> listener = new ArrayList<>();
	private ThumbnailQueue queue = new ThumbnailQueue();

	private ThumbnailHandler(String temporaryFolder) {
		if (!temporaryFolder.endsWith(File.separator))
			temporaryFolder += File.separator;
		thumbnails = new File(temporaryFolder + THUMBNAILS);
		if (!thumbnails.exists())
			thumbnails.mkdir();
		queue.start();
	}

	public List<ThumbnailListener> calculationListener() {
		return listener;
	}

	public void informListener(ThumbnailJob job) {
		for (ThumbnailListener l : listener) {
			try {
				l.onThumbnailCalculation(job);
			} catch (Exception e) {
			}
		}
	}

	private File getThumbnailFile(File file, int w, int h) {
		String path = thumbnails.getAbsolutePath() + File.separator
				+ file.getAbsolutePath().replace(File.separator, "_")
				+ file.getAbsolutePath().hashCode() + w + 'x' + h + ".thumb";
		return new File(path);
	}

	public static class Thumbnail {

		public int width;
		public int height;
		public int[] rgb;
	}

	public void manageImageThumbnail(File file, int width, int height) {
		ImageThumbnailJob job = new ImageThumbnailJob(file, width, height);

		queueThumbnailJob(job);
	}

	public void queueThumbnailJob(ThumbnailJob job) {
		if (job.needsCalculation()) {
			for (ThumbnailJob j : queue.jobs) {
				if (job.equals(j))
					return;
			}
			queue.queueJob(job);
		} else {
			job.readThumbnail();
			informListener(job);
		}
	}

	private Thumbnail createThumbnail(File file, File thumbnailFile, int width,
			int height) {
		try {
			BufferedImage src = ImageIO.read(file);
			Thumbnail thumbnailData = new Thumbnail();
			BufferedImage thumbnail = toBufferedImage(createThumbnail(src,
					width));
			thumbnailData.width = thumbnail.getWidth();
			thumbnailData.height = thumbnail.getHeight();
			thumbnailData.rgb = ((DataBufferInt) thumbnail.getData()
					.getDataBuffer()).getData();
			thumbnailData.rgb = compressRGB565(thumbnailData.rgb,
					thumbnailData.width, thumbnailData.height);
			writeThumbnail(thumbnailFile, thumbnailData);
			return thumbnailData;
		} catch (Exception e) {
			file.getName();
		}
		return null;
	}

	void writeThumbnail(File thumbnailFile, Thumbnail thumbnailData)
			throws IOException {
		DataOutputStream stream = new DataOutputStream(new FileOutputStream(
				thumbnailFile));
		stream.writeInt(thumbnailData.width);
		stream.writeInt(thumbnailData.height);
		for (int i = 0; i < thumbnailData.rgb.length; i++)
			stream.writeInt(thumbnailData.rgb[i]);
		stream.close();
	}

	BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null),
				img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bimage;
	}

	public Image createThumbnail(BufferedImage sourceImage, int size) {
		int width = sourceImage.getWidth();
		int height = sourceImage.getHeight();
		int bigSize = Math.min(width, height);
		int diffWidth = width - bigSize;
		int diffHeight = height - bigSize;
		BufferedImage bigImg = sourceImage.getSubimage(diffWidth / 2,
				diffHeight / 2, bigSize, bigSize);
		Image smallImg = bigImg.getScaledInstance(Math.min(size, width),
				Math.min(size, height), Image.SCALE_SMOOTH);
		return smallImg;
	}

	private static int compressRGB565(int i) {
		int ret = 0;
		// blue 5 bit
		ret |= (0x000000F8 & i) >> 3;
		// green 6 bit
		ret |= (0x0000FC00 & i) >> 2 + 3;
		// red 5 bit
		ret |= (0x00F80000 & i) >> 3 + 2 + 3;
		return ret;
	}

	public static int[] compressRGB565(int[] rgb, int width, int height) {
		int[] ret = new int[width * height / 2];
		for (int j = 0; j < height; j++)
			for (int i = 0; i < width; i += 2) {
				int px1 = compressRGB565(rgb[j * width + i]);
				int px2 = compressRGB565(rgb[j * width + i + 1]);
				ret[j * width / 2 + i / 2] = px1 | (px2 << 16);
			}
		return ret;
	}

	public int[] readImageIntArray(BufferedImage image) {
		int rgb[] = new int[image.getWidth() * image.getHeight()];
		for (int i = 0; i < rgb.length; i++)
			rgb[i] = image.getRGB(i % image.getWidth(), i / image.getWidth());
		return rgb;
	}

	private Thumbnail readThumbnail(File thumbnailFile) {
		try {
			Thumbnail data = new Thumbnail();
			DataInputStream input = new DataInputStream(new FileInputStream(
					thumbnailFile));
			data.width = input.readInt();
			data.height = input.readInt();
			data.rgb = new int[data.width * data.height / 2];
			for (int i = 0; i < data.rgb.length; i++)
				data.rgb[i] = input.readInt();
			input.close();
			return data;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Thumbnail searchStringThumbnail(String string) {
		Thumbnail thumbnail = null;
		try {
			File thumbnailFile = getStringThumbnailFile(string);
			if (thumbnailFile.exists())
				thumbnail = readThumbnail(thumbnailFile);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return thumbnail;
	}

	File getStringThumbnailFile(String string)
			throws UnsupportedEncodingException {
		String path = thumbnails.getAbsolutePath() + File.separator + "string_"
				+ URLEncoder.encode(string, "UTF-8");
		return new File(path);
	}

	public interface ThumbnailListener {
		public void onThumbnailCalculation(ThumbnailJob job);
	}

	private class ThumbnailQueue extends Thread {

		private List<ThumbnailJob> jobs = Collections
				.synchronizedList(new LinkedList<ThumbnailJob>());

		@Override
		synchronized public void run() {
			while (true) {
				if (jobs.size() > 0) {
					ThumbnailJob job = jobs.get(0);
					jobs.remove(0);
					job.calculateThumbnail();
					instance().informListener(job);
				} else {
					try {
						this.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		}

		synchronized public void queueJob(ThumbnailJob job) {
			jobs.add(job);
			this.notify();
		}

	}

	public static abstract class ThumbnailJob {

		public Thumbnail thumbnail;

		protected abstract void calculateThumbnail();

		protected abstract Thumbnail readThumbnail();

		protected abstract boolean needsCalculation();

	}

	public static class ImageThumbnailJob extends ThumbnailJob {

		private File imageFile;
		private int width;
		private int height;

		public ImageThumbnailJob(File imageFile, int width, int height) {
			this.imageFile = imageFile;
			this.width = width;
			this.height = height;
		}

		@Override
		protected void calculateThumbnail() {
			File thumbnailFile = instance().getThumbnailFile(imageFile, width,
					height);
			if (!thumbnailFile.exists())
				thumbnail = instance().createThumbnail(imageFile,
						thumbnailFile, width, height);
			else
				thumbnail = instance().readThumbnail(thumbnailFile);
		}

		@Override
		protected Thumbnail readThumbnail() {
			File thumbnailFile = instance().getThumbnailFile(imageFile, width,
					height);
			if (thumbnailFile.exists()) {
				thumbnail = instance().readThumbnail(thumbnailFile);
				return thumbnail;
			}
			return null;
		}

		@Override
		protected boolean needsCalculation() {
			File thumbnailFile = instance().getThumbnailFile(imageFile, width,
					height);
			return !thumbnailFile.exists();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ImageThumbnailJob) {
				ImageThumbnailJob img = (ImageThumbnailJob) obj;
				return img.imageFile.getAbsoluteFile().equals(
						imageFile.getAbsoluteFile())
						&& width == img.width && height == img.height;
			}
			return super.equals(obj);
		}

	}
}
