package de.neo.smarthome.mediaserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.api.PlayerException;

public class PlayList {

	private String mPlaylistLocation;

	public PlayList(String playlistLocation) {
		this.mPlaylistLocation = playlistLocation;
		if (playlistLocation != null && !playlistLocation.endsWith(File.separator))
			this.mPlaylistLocation += File.separator;
	}

	public String[] getPlayLists() throws RemoteException {
		List<String> plsList = new ArrayList<String>();
		File playlistFolder = new File(mPlaylistLocation);
		if (!playlistFolder.exists())
		{
			throw new RemoteException("Playlist folder does not exist!");
		}
		for (File pls : playlistFolder.listFiles()) {
			String n = pls.getName();
			if (!n.startsWith(".") && n.length() > 4)
				plsList.add(n.substring(0, n.length() - 4));
		}
		return plsList.toArray(new String[plsList.size()]);
	}

	public void addPlayList(String name) throws RemoteException {
		File pls = new File(mPlaylistLocation + name + ".pls");
		try {
			pls.createNewFile();
		} catch (IOException e) {
			RemoteLogger.performLog(LogPriority.ERROR, e.getClass().getSimpleName() + ": " + e.getMessage(),
					"Mediaserver");
		}
	}

	public void extendPlayList(String pls, String file) throws RemoteException, PlayerException {
		try {
			File plsF = new File(mPlaylistLocation + pls + ".pls");
			PrintStream fileStream = new PrintStream(new FileOutputStream(plsF, true));
			File fileF = new File(file);
			if (!fileF.exists()) {
				fileStream.close();
				throw new PlayerException("the file '" + file + "' does not exist");
			}
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

	public String[] listContent(String pls) throws RemoteException, PlayerException {
		File plsF = new File(mPlaylistLocation + pls + ".pls");
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

	public void removePlayList(String pls) throws RemoteException {
		new File(mPlaylistLocation + pls + ".pls").delete();
	}

	public void renamePlayList(String oldPls, String newPls) throws RemoteException {
		new File(mPlaylistLocation + oldPls + ".pls").renameTo(new File(mPlaylistLocation + newPls + ".pls"));
	}

	public void removeItem(String pls, String item) throws RemoteException, PlayerException {
		try {
			File plsF = new File(mPlaylistLocation + pls + ".pls");
			List<String> files = new ArrayList<String>();
			BufferedReader reader;
			reader = new BufferedReader(new FileReader(plsF));
			String f = null;
			while ((f = reader.readLine()) != null)
				if (!f.endsWith(item))
					files.add(f);
			reader.close();
			PrintStream fileStream = new PrintStream(new FileOutputStream(plsF, false));
			for (String i : files)
				fileStream.println(i);
			fileStream.flush();
			fileStream.close();
		} catch (FileNotFoundException e) {
			throw new PlayerException("Playlist '" + pls + "' does not exist");
		} catch (IOException e) {
			RemoteLogger.performLog(LogPriority.ERROR, e.getClass().getSimpleName() + ": " + e.getMessage(),
					"Mediaserver");
		}
	}

	public String getPlaylistFullpath(String pls) throws RemoteException {
		return mPlaylistLocation + pls + ".pls";
	}

}
