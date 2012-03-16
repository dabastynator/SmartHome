package de.remote.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IPlayList;
import de.remote.api.PlayerException;

public class PlayListImpl implements IPlayList {

	public static final String PLAYLIST_LOCATION = "/home/sebastian/temp/playlists/";

	@Override
	public String[] getPlayLists() throws RemoteException {
		List<String> plsList = new ArrayList<String>();
		File playlistFolder = new File(PLAYLIST_LOCATION);
		for (File pls : playlistFolder.listFiles()) {
			String n = pls.getName();
			plsList.add(n.substring(0, n.length() - 4));
		}
		return plsList.toArray(new String[plsList.size()]);
	}

	@Override
	public void addPlayList(String name) throws RemoteException {
		File pls = new File(PLAYLIST_LOCATION + name + ".pls");
		try {
			pls.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void extendPlayList(String pls, String file) throws RemoteException,
			PlayerException {
		try {
			File plsF = new File(PLAYLIST_LOCATION + pls + ".pls");
			PrintStream fileStream = new PrintStream(new FileOutputStream(plsF,
					true));
			File fileF = new File(file);
			if (!fileF.exists())
				throw new PlayerException("the file '" + file
						+ "' does not exist");
			if (fileF.isDirectory()) {
				for (File f : fileF.listFiles())
					fileStream.println(f.getAbsolutePath());
			} else {
				fileStream.println(file);
			}
			fileStream.flush();
			fileStream.close();
		} catch (FileNotFoundException e) {
			throw new PlayerException("playlist '" + pls + "' does not exist");
		}
	}

	@Override
	public String[] listContent(String pls) throws RemoteException,
			PlayerException {
		File plsF = new File(PLAYLIST_LOCATION + pls + ".pls");
		List<String> files = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(plsF));
			String f = null;
			while ((f = reader.readLine()) != null)
				files.add(f);
			reader.close();
		} catch (FileNotFoundException e) {
			throw new PlayerException("playlist '" + pls + "' does not exist");
		} catch (IOException e) {
			throw new PlayerException(e.getMessage());
		}
		return files.toArray(new String[files.size()]);
	}

	@Override
	public void removePlayList(String pls) throws RemoteException {
		new File(PLAYLIST_LOCATION + pls + ".pls").delete();
	}

	@Override
	public void renamePlayList(String oldPls, String newPls)
			throws RemoteException {
		new File(PLAYLIST_LOCATION + oldPls + ".pls").renameTo(new File(
				PLAYLIST_LOCATION + newPls + ".pls"));
	}

	@Override
	public void removeItem(String pls, String item) throws RemoteException,
			PlayerException {
		try {
			File plsF = new File(PLAYLIST_LOCATION + pls + ".pls");
			List<String> files = new ArrayList<String>();
			BufferedReader reader;
			reader = new BufferedReader(new FileReader(plsF));
			String f = null;
			while ((f = reader.readLine()) != null)
				if (!f.endsWith(item))
					files.add(f);
			reader.close();
			PrintStream fileStream = new PrintStream(new FileOutputStream(plsF,
					false));
			for (String i : files)
				fileStream.println(i);
			fileStream.flush();
			fileStream.close();
		} catch (FileNotFoundException e) {
			throw new PlayerException("Playlist '" + pls + "' does not exist");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
