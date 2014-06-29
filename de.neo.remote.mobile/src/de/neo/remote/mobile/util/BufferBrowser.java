package de.neo.remote.mobile.util;

import java.io.IOException;
import java.util.Arrays;

import de.neo.remote.mediaserver.api.IBrowser;
import de.neo.remote.mediaserver.api.IThumbnailListener;
import de.neo.rmi.protokol.RemoteException;
import de.neo.rmi.protokol.ServerPort;

/**
 * this proxy buffers temporary information about the current directory.
 * 
 * @author sebastian
 */
public class BufferBrowser implements IBrowser {

	private IBrowser browser;
	private boolean isDirtyDirectory;
	private boolean isDirtyFile;
	private String[] directories;
	private String[] files;
	private boolean isDirtyLocation;
	private String location;
	private boolean isDirtyFullLocation;
	private String fullLocation;

	/**
	 * allocates new buffered browser.
	 * 
	 * @param browser
	 */
	public BufferBrowser(IBrowser browser) {
		this.browser = browser;
		isDirtyDirectory = true;
		isDirtyFile = true;
		isDirtyLocation = true;
		isDirtyFullLocation = true;
	}

	@Override
	public boolean goBack() throws RemoteException {
		isDirtyDirectory = true;
		isDirtyFile = true;
		isDirtyLocation = true;
		isDirtyFullLocation = true;
		return browser.goBack();
	}

	@Override
	public void goTo(String directory) throws RemoteException {
		isDirtyDirectory = true;
		isDirtyFile = true;
		isDirtyLocation = true;
		isDirtyFullLocation = true;
		browser.goTo(directory);
	}

	@Override
	public String[] getDirectories() throws RemoteException {
		if (isDirtyDirectory) {
			directories = browser.getDirectories();
			Arrays.sort(directories);
		}
		isDirtyDirectory = false;
		return directories;
	}

	@Override
	public String[] getFiles() throws RemoteException {
		if (isDirtyFile) {
			files = browser.getFiles();
			Arrays.sort(files);
		}
		isDirtyFile = false;
		return files;
	}

	@Override
	public String getLocation() throws RemoteException {
		if (isDirtyLocation)
			location = browser.getLocation();
		isDirtyLocation = false;
		return location;
	}

	@Override
	public String getFullLocation() throws RemoteException {
		if (isDirtyFullLocation)
			fullLocation = browser.getFullLocation();
		isDirtyFullLocation = false;
		return fullLocation;
	}

	@Override
	public boolean delete(String file) throws RemoteException {
		return browser.delete(file);
	}

	/**
	 * set buffered browser dirty. to force to get all information from the
	 * remote browser
	 */
	public void setDirty() {
		isDirtyDirectory = true;
		isDirtyFile = true;
		isDirtyFullLocation = true;
		isDirtyLocation = true;
	}

	@Override
	public ServerPort publishFile(String file) throws RemoteException,
			IOException {
		return browser.publishFile(file);
	}

	@Override
	public ServerPort publishDirectory(String directory)
			throws RemoteException, IOException {
		return browser.publishDirectory(directory);
	}

	@Override
	public void updloadFile(String file, String serverIp, int port)
			throws RemoteException {
		browser.updloadFile(file, serverIp, port);
	}

	@Override
	public void fireThumbnails(IThumbnailListener listener, int width,
			int height) throws RemoteException {
		browser.fireThumbnails(listener, width, height);
	}

	@Override
	public ServerPort publishAbsoluteFile(String string)
			throws RemoteException, IOException {
		return browser.publishAbsoluteFile(string);
	}

	public String[] getDirectoriesInt() {
		return directories;
	}

	public String getLocationInt() {
		if (isDirtyLocation)
			return "no connection";
		return location;
	}

}
