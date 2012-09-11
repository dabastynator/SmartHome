package de.remote.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.protokol.RemoteException;
import de.newsystem.rmi.transceiver.DirectorySender;
import de.newsystem.rmi.transceiver.FileReceiver;
import de.newsystem.rmi.transceiver.FileSender;
import de.remote.api.IBrowser;

public class BrowserImpl implements IBrowser {

	private String location;
	private String root;

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
		DirectorySender sender = new DirectorySender(new File(location + directory), port, 1);
		sender.sendAsync();
		return Server.getServer().getServerPort().getIp();
	}

	@Override
	public void updloadFile(String file, String serverIp, int port)
			throws RemoteException {
		FileReceiver receiver = new FileReceiver(serverIp, port, new File(location + file));
		receiver.receiveAsync();
	}

}
