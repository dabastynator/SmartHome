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

	private ThumbnailHandler(String temporaryFolder) {
		if (!temporaryFolder.endsWith(File.separator))
			temporaryFolder += File.separator;
		thumbnails = new File(temporaryFolder + THUMBNAILS);
		if (!thumbnails.exists())
			thumbnails.mkdir();
	}

	private File getThumbnailFile(File file, int w, int h) {
		String path = thumbnails.getAbsolutePath() + File.separator
				+ file.getAbsolutePath().replace(File.separator, "_")
				+ file.getAbsolutePath().hashCode() + w + 'x' + h + ".thumb";
		return new File(path);
	}

	public class Thumbnail {

		public int width;
		public int height;
		public int[] rgb;
	}

	public Thumbnail manageImageThumbnail(File file, int width, int height) {
		Thumbnail thumbnail = null;
		File thumbnailFile = getThumbnailFile(file, width, height);
		if (!thumbnailFile.exists())
			thumbnail = createThumbnail(file, thumbnailFile, width, height);
		else
			thumbnail = readThumbnail(thumbnailFile);
		return thumbnail;
	}

	public Thumbnail manageStringThumbnail(String string, BufferedImage image,
			int size) {
		Thumbnail thumbnail = new Thumbnail();
		try {
			File thumbnailFile = getStringThumbnailFile(string);
			BufferedImage thumbnailImage = toBufferedImage(createThumbnail(
					image, size));
			int rgb[] = readImageIntArray(thumbnailImage);
			rgb = compressRGB565(rgb, size, size);
			thumbnail.width = size;
			thumbnail.height = size;
			thumbnail.rgb = rgb;
			writeThumbnail(thumbnailFile, thumbnail);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return thumbnail;
	}

	private Thumbnail createThumbnail(File file, File thumbnailFile, int width,
			int height) {
		try {
			BufferedImage src = ImageIO.read(file);
			Thumbnail thumbnailData = new Thumbnail();
			double radio = Math.min(width / (double) src.getWidth(), height
					/ (double) src.getHeight());
			thumbnailData.width = (int) (radio * src.getWidth());
			thumbnailData.height = (int) (radio * src.getHeight());
			if (thumbnailData.width % 2 != 0)
				thumbnailData.width++;
			BufferedImage thumbnail = toBufferedImage(createThumbnail(src,
					thumbnailData.width));
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

	private void writeThumbnail(File thumbnailFile, Thumbnail thumbnailData)
			throws IOException {
		DataOutputStream stream = new DataOutputStream(new FileOutputStream(
				thumbnailFile));
		stream.writeInt(thumbnailData.width);
		stream.writeInt(thumbnailData.height);
		for (int i = 0; i < thumbnailData.rgb.length; i++)
			stream.writeInt(thumbnailData.rgb[i]);
		stream.close();
	}

	private BufferedImage toBufferedImage(Image img) {
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

	private Image createThumbnail(BufferedImage sourceImage, int size) {
		int width = sourceImage.getWidth();
		int height = sourceImage.getHeight();

		if (width > height) {
			float extraSize = height - size;
			float percentHight = (extraSize / height) * size;
			float percentWidth = width - ((width / size) * percentHight);
			BufferedImage img = new BufferedImage((int) percentWidth, size,
					BufferedImage.TYPE_INT_RGB);
			Image scaledImage = sourceImage.getScaledInstance(
					(int) percentWidth, size, Image.SCALE_SMOOTH);
			img.createGraphics().drawImage(scaledImage, 0, 0, null);
			BufferedImage img2 = new BufferedImage(size, size,
					BufferedImage.TYPE_INT_RGB);
			img2 = img.getSubimage((int) ((percentWidth - size) / 2), 0, size,
					size);
			return img2;
		} else {
			float extraSize = width - size;
			float percentWidth = (extraSize / width) * size;
			float percentHight = height - ((height / size) * percentWidth);
			BufferedImage img = new BufferedImage(size, (int) percentHight,
					BufferedImage.TYPE_INT_RGB);
			Image scaledImage = sourceImage.getScaledInstance(size,
					(int) percentHight, Image.SCALE_SMOOTH);
			img.createGraphics().drawImage(scaledImage, 0, 0, null);
			BufferedImage img2 = new BufferedImage(size, size,
					BufferedImage.TYPE_INT_RGB);
			img2 = img.getSubimage(0, (int) ((percentHight - size) / 2), size,
					size);
			return img2;
		}
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

	private int[] readImageIntArray(BufferedImage image) {
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

	private File getStringThumbnailFile(String string)
			throws UnsupportedEncodingException {
		String path = "string_" + URLEncoder.encode(string, "UTF-8");
		return new File(path);
	}

}
