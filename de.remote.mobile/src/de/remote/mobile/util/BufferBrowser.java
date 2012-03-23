package de.remote.mobile.util;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IBrowser;

/**
 * this proxy buffers temporary information about the current directory.
 * @author sebastian
 */
public class BufferBrowser implements IBrowser{

	/**
	 * browser object
	 */
	private IBrowser browser;
	
	/**
	 * state of directory 
	 */
	private boolean isDirtyDirectory;
	
	/**
	 * state of file 
	 */
	private boolean isDirtyFile;
	
	/**
	 * temporary directories
	 */
	private String[] directories;
	
	/**
	 * temporary files 
	 */
	private String[] files;
	
	/**
	 * state of location 
	 */
	private boolean isDirtyLocation;
	
	/**
	 * temporary location 
	 */
	private String location;
	
	/**
	 * state of full location 
	 */
	private boolean isDirtyFullLocation;
	
	/**
	 * temporary full location 
	 */
	private String fullLocation;

	/**
	 * allocates new buffered browser.
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
		if (isDirtyDirectory)
			directories = browser.getDirectories();
		isDirtyDirectory = false;
		return directories;
	}

	@Override
	public String[] getFiles() throws RemoteException {
		if (isDirtyFile)
			files = browser.getFiles();
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

}
