package de.neo.smarthome.api;

import java.io.IOException;
import java.util.ArrayList;

import de.neo.persist.DaoException;
import de.neo.remote.rmi.RemoteAble;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebField;
import de.neo.remote.web.WebParam;
import de.neo.remote.web.WebRequest;

public interface IWebMediaServer extends RemoteAble
{

	public enum FileType
	{
		Directory, File
	}

	public static final String MPlayer = "mplayer";
	public static final String OMXPlayer = "omxplayer";
	public static final String TOTEM = "totem";

	@WebRequest(path = "list", description = "List all registered media-server with current playing state. Optional parameter id specified required media-server.", genericClass = BeanMediaServer.class)
	public ArrayList<BeanMediaServer> getMediaServer(
			@WebParam(name = "token") String token,
			@WebParam(name = "id", required = false, defaultvalue = "") String id)
					throws RemoteException;

	@WebRequest(path = "playlists", description = "List all playlists of specified media server.", genericClass = BeanPlaylist.class)
	public ArrayList<BeanPlaylist> getPlaylists(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id)
					throws RemoteException;

	@WebRequest(path = "playlist_extend", description = "Add item to specified playlist.")
	public void playlistExtend(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "playlist") String playlist,
			@WebParam(name = "item") String item)
					throws RemoteException, PlayerException;

	@WebRequest(path = "playlist_create", description = "Create new playlist.")
	public void playlistCreate(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "playlist") String playlist) 
					throws RemoteException;

	@WebRequest(path = "playlist_delete", description = "Delete specified playlist.")
	public void playlistDelete(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "playlist") String playlist) 
					throws RemoteException, PlayerException;

	@WebRequest(path = "playlist_delete_item", description = "Delete item from specified playlist.")
	public void playlistDeleteItem(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "playlist") String playlist, 
			@WebParam(name = "item") String item)
					throws RemoteException, PlayerException;

	@WebRequest(path = "playlist_content", description = "List items of specified playlist.", genericClass = BeanPlaylistItem.class)
	public ArrayList<BeanPlaylistItem> getPlaylistContent(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id, 
			@WebParam(name = "playlist") String playlist)
					throws RemoteException, PlayerException;

	@WebRequest(path = "files", description = "Get files and directories at specific path.", genericClass = BeanFileSystem.class)
	public ArrayList<BeanFileSystem> getFiles(
			@WebParam(name = "token") String token, 
			@WebParam(name = "id") String id,
			@WebParam(name = "path", required = false, defaultvalue = "") String path) 
					throws RemoteException;
	
	@WebRequest(path = "search", description = "Search for files and directories at specific path.", genericClass = BeanFileSystem.class)
	public ArrayList<BeanFileSystem> searchFiles(
			@WebParam(name = "token") String token, 
			@WebParam(name = "id") String id,
			@WebParam(name = "target") String target,
			@WebParam(name = "path", required = false, defaultvalue = "") String path) 
					throws RemoteException;

	@WebRequest(path = "play_file", description = "Play specified file or directory.")
	public PlayingBean playFile(
			@WebParam(name = "token") String token, 
			@WebParam(name = "id") String id,
			@WebParam(name = "player") String player, 
			@WebParam(name = "file") String file)
					throws RemoteException, PlayerException;

	@WebRequest(path = "play_playlist", description = "Play specified playlist.")
	public PlayingBean playPlaylist(
			@WebParam(name = "token") String token, 
			@WebParam(name = "id") String id,
			@WebParam(name = "player") String player, 
			@WebParam(name = "playlist") String playlist)
					throws RemoteException, PlayerException;

	@WebRequest(path = "play_pause", description = "Change state of player. Plaing -> Pause and Pause -> Playing. If player does not play anything, throw player exception.")
	public PlayingBean playPause(
			@WebParam(name = "token") String token, 
			@WebParam(name = "id") String id,
			@WebParam(name = "player") String player) 
					throws RemoteException, PlayerException;

	@WebRequest(path = "next", description = "Play next file in playlist or filesystem.")
	public PlayingBean playNext(
			@WebParam(name = "token") String token, 
			@WebParam(name = "id") String id,
			@WebParam(name = "player") String player) 
					throws RemoteException, PlayerException;

	@WebRequest(path = "previous", description = "Play previous file in playlist or filesystem.")
	public PlayingBean playPrevious(
			@WebParam(name = "token") String token, 
			@WebParam(name = "id") String id,
			@WebParam(name = "player") String player) 
					throws RemoteException, PlayerException;

	@WebRequest(path = "seek_forward", description = "Seek forward in current playing.")
	public PlayingBean playSeekForward(
			@WebParam(name = "token") String token, 
			@WebParam(name = "id") String id,
			@WebParam(name = "player") String player) 
					throws RemoteException, PlayerException;

	@WebRequest(path = "seek_backward", description = "Seek backward in current playing.")
	public PlayingBean playSeekBackward(
			@WebParam(name = "token") String token, 
			@WebParam(name = "id") String id,
			@WebParam(name = "player") String player) 
					throws RemoteException, PlayerException;

	@WebRequest(path = "seek", description = "Seek to specific time in current playing.")
	public PlayingBean playSeek(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "player") String player,
			@WebParam(name = "seek_time_sec") int timeSec)
					throws RemoteException, PlayerException;

	@WebRequest(path = "set_fullscreen", description = "Set the specified player in fullsceen mode or change to windowed mode.")
	public PlayingBean playSetFullscreen(
			@WebParam(name = "token") String token, 
			@WebParam(name = "id") String id,
			@WebParam(name = "player") String player, 
			@WebParam(name = "fullscreen") boolean fullscreen)
					throws RemoteException, PlayerException;

	@WebRequest(path = "stop", description = "Stop playing.")
	public PlayingBean playStop(
			@WebParam(name = "token") String token, 
			@WebParam(name = "id") String id,
			@WebParam(name = "player") String player) 
					throws RemoteException, PlayerException;

	@WebRequest(path = "volume", description = "Set volume of the player. Value must between 0 and 100.")
	public PlayingBean setVolume(
			@WebParam(name = "token") String token, 
			@WebParam(name = "id") String id,
			@WebParam(name = "player") String player, 
			@WebParam(name = "volume") int volume)
					throws RemoteException, PlayerException;

	@WebRequest(path = "delta_volume", description = "Change volume of the player by given delta.")
	public PlayingBean setDeltaVolume(
			@WebParam(name = "token") String token, 
			@WebParam(name = "id") String id,
			@WebParam(name = "player") String player, 
			@WebParam(name = "delta") int delta)
					throws RemoteException, PlayerException;

	@WebRequest(path = "create", description = "Create new media server.")
	public BeanMediaServer createNewMediaServer(
			@WebParam(name = "token") String token, 
			@WebParam(name = "id") String id,
			@WebParam(name = "name") String name, 
			@WebParam(name = "location_browser") String locationBrowser,
			@WebParam(name = "location_playlist") String locationPlaylist,
			@WebParam(name = "description") String description) 
					throws RemoteException, IOException, DaoException;

	@WebRequest(path = "update", description = "Update existing media server.")
	public BeanMediaServer updateExistingMediaServer(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "name") String name,
			@WebParam(name = "location_browser") String locationBrowser,
			@WebParam(name = "location_playlist") String locationPlaylist,
			@WebParam(name = "description") String description) 
					throws RemoteException, IOException, DaoException;

	@WebRequest(path = "delete", description = "Delete media server.")
	public void deleteMediaServer(
			@WebParam(name = "token") String token, 
			@WebParam(name = "id") String id)
					throws RemoteException, IOException, DaoException;

	public static class BeanFileSystem implements Comparable<BeanFileSystem>
	{

		@WebField(name = "filetype")
		public FileType fileType;

		@WebField(name = "name")
		public String name;
		
		@WebField(name = "path")
		public String path;

		@Override
		public int compareTo(BeanFileSystem another)
		{
			if (another == null)
				return 0;
			if (fileType == another.fileType)
				return name.compareToIgnoreCase(another.name);
			if (fileType == FileType.Directory)
				return -1;
			return 1;
		}
	}

	public static class BeanPlaylist implements Comparable<BeanPlaylist>
	{

		@WebField(name = "name")
		public String name;

		@WebField(name = "item_count")
		public int itemCount;

		@Override
		public int compareTo(BeanPlaylist another)
		{
			return name.compareToIgnoreCase(another.name);
		}

	}

	public static class BeanPlaylistItem
	{

		@WebField(name = "name")
		public String name;

		@WebField(name = "path")
		public String path;

	}

	public class BeanMediaServer extends BeanWeb 
	{

		@WebField(name = "current_playing")
		public PlayingBean currentPlaying;

	}

}
