package de.neo.remote.mediaserver.impl;

import java.io.File;
import java.io.IOException;

import de.neo.remote.mediaserver.api.IImageViewer;
import de.neo.remote.mediaserver.api.ImageException;
import de.neo.rmi.protokol.RemoteException;

public class ImageViewerImpl implements IImageViewer {

	public static final String IMAGE_VIEWER = "/usr/bin/eog";

	public static final String[] IMAGE_EXTENSIONS = { "jpg", "jpeg", "png",
			"gif", "tiff", "bmp" };

	private File[] currentImageFolder;

	private int currentImageIndex;

	private Process viewerProgress;

	private boolean isImage(String file) {
		boolean isImage = false;
		String[] split = file.split("\\.");
		String fileExtension = split[split.length - 1].toLowerCase();
		for (String extension : IMAGE_EXTENSIONS) {
			isImage |= extension.equals(fileExtension);
		}
		return isImage;
	}

	@Override
	public void show(String file) throws RemoteException, ImageException {
		File currentImage = new File(file);
		if (!currentImage.exists())
			throw new ImageException("Image does not exist: " + file);
		if (!isImage(file))
			throw new ImageException("Unknown image-extension: " + file);
		try {
			if (viewerProgress != null)
				viewerProgress.destroy();
			viewerProgress = Runtime.getRuntime().exec(
					new String[] { IMAGE_VIEWER, "-f", file });
		} catch (IOException e) {
			throw new ImageException(e.getMessage());
		}
		currentImageFolder = new File(currentImage.getParent()).listFiles();
		for (int i = 0; i < currentImageFolder.length; i++) {
			if (currentImageFolder[i].getAbsolutePath().equals(file))
				currentImageIndex = i;
		}
	}

	@Override
	public void quit() throws RemoteException, ImageException {
		if (viewerProgress == null)
			throw new ImageException("Image-viewer is down");
		else {
			viewerProgress.destroy();
			viewerProgress = null;
			currentImageFolder = null;
		}
	}

	@Override
	public void toggleDiashow(int imageTime) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void next() throws RemoteException, ImageException {
		for (int i = 1; i <= currentImageFolder.length; i++) {
			String file = currentImageFolder[(currentImageIndex + i)
					% currentImageFolder.length].getAbsolutePath();
			if (isImage(file)) {
				currentImageIndex = (currentImageIndex + i)
						% currentImageFolder.length;
				if (viewerProgress != null)
					viewerProgress.destroy();
				try {
					viewerProgress = Runtime.getRuntime().exec(
							new String[] { IMAGE_VIEWER, "-f", file });
				} catch (IOException e) {
					throw new ImageException(e.getMessage());
				}
				return;
			}
		}
	}

	@Override
	public void previous() throws RemoteException, ImageException {
		for (int i = -1; -i <= currentImageFolder.length; i--) {
			String file = currentImageFolder[(currentImageIndex + i + currentImageFolder.length)
					% currentImageFolder.length].getAbsolutePath();
			if (isImage(file)) {
				currentImageIndex = (currentImageIndex + i + currentImageFolder.length)
						% currentImageFolder.length;
				if (viewerProgress != null)
					viewerProgress.destroy();
				try {
					viewerProgress = Runtime.getRuntime().exec(
							new String[] { IMAGE_VIEWER, "-f", file });
				} catch (IOException e) {
					throw new ImageException(e.getMessage());
				}
				return;
			}
		}
	}

	public static void main(String[] args) {
		ImageViewerImpl viewer = new ImageViewerImpl();
		try {
			viewer.show("/home/sebastian/Bilder/bild.png");
			viewer.next();
			viewer.next();
			viewer.previous();
			viewer.previous();
			viewer.quit();
			viewer.quit();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ImageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
