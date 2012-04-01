package de.remote.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IBrowser;

public class BrowserImpl implements IBrowser {

	private String location;
	private String root;

	public BrowserImpl(String string) {
		root = location = string;
	}

	@Override
	public boolean goBack() {
		if (root.equals(location))
			return false;
		location = location.substring(0, location.lastIndexOf("/"));
		location = location.substring(0, location.lastIndexOf("/") + 1);
		return true;
	}

	@Override
	public void goTo(String directory) {
		location += directory + "/";
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
		if (location.lastIndexOf('/') >= 0) {
			String str = location.substring(0, location.lastIndexOf('/'));
			return str.substring(str.lastIndexOf('/') + 1);
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

}
