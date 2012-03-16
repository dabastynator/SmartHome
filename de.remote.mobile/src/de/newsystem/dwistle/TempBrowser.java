package de.newsystem.dwistle;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IBrowser;

public class TempBrowser implements IBrowser{

	private IBrowser browser;
	private boolean isDirtyDirectory;
	private boolean isDirtyFile;
	private String[] directories;
	private String[] files;
	private boolean isDirtyLocation;
	private String location;
	private boolean isDirtyFullLocation;
	private String fullLocation;

	public TempBrowser(IBrowser browser) {
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
