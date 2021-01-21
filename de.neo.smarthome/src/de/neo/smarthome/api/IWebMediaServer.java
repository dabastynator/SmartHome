package de.neo.smarthome.api;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import de.neo.persist.DaoException;
import de.neo.remote.rmi.RemoteAble;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebField;
import de.neo.remote.web.WebGet;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.api.IControlCenter.BeanWeb;

public interface IWebMediaServer extends RemoteAble {

	public enum FileType {
		Directory, File
	}

	public static final String FileSeparator = "<->";
	public static final String MPlayer = "mplayer";
	public static final String OMXPlayer = "omxplayer";
	public static final String TOTEM = "totem";

	/**
	 * List all registered media-server with current playing state. Optional
	 * parameter id specified required media-server.
	 *
	 * @param id
	 * @return media-server list
	 */
	@WebRequest(path = "list", description = "List all registered media-server with current playing state. Optional parameter id specified required media-server.", genericClass = BeanMediaServer.class)
	public ArrayList<BeanMediaServer> getMediaServer(@WebGet(name = "token") String token,
			@WebGet(name = "id", required = false, defaultvalue = "") String id) throws RemoteException;

	/**
	 * List all playlists of specified media server.
	 *
	 * @param id
	 * @return playlists
	 */
	@WebRequest(path = "playlists", description = "List all playlists of specified media server.", genericClass = BeanPlaylist.class)
	public ArrayList<BeanPlaylist> getPlaylists(@WebGet(name = "token") String token, @WebGet(name = "id") String id)
			throws RemoteException;

	/**
	 * Add item to specified playlist.
	 *
	 * @param id
	 * @param playlist
	 * @param item
	 * @throws RemoteException
	 */
	@WebRequest(path = "playlist_extend", description = "Add item to specified playlist.")
	public void playlistExtend(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "playlist") String playlist, @WebGet(name = "item") String item)
			throws RemoteException, PlayerException;

	/**
	 * Create new playlist.
	 *
	 * @param id
	 * @param playlist
	 */
	@WebRequest(path = "playlist_create", description = "Create new playlist.")
	public void playlistCreate(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "playlist") String playlist) throws RemoteException;

	/**
	 * Delete specified playlist
	 *
	 * @param id
	 * @param playlist
	 * @throws RemoteException
	 */
	@WebRequest(path = "playlist_delete", description = "Delete specified playlist.")
	public void playlistDelete(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "playlist") String playlist) throws RemoteException, PlayerException;

	/**
	 * Delete item from specified playlist.
	 *
	 * @param id
	 * @param playlist
	 * @param item
	 * @throws RemoteException
	 */
	@WebRequest(path = "playlist_delete_item", description = "Delete item from specified playlist.")
	public void playlistDeleteItem(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "playlist") String playlist, @WebGet(name = "item") String item)
			throws RemoteException, PlayerException;

	/**
	 * List items of specified playlist.
	 *
	 * @param id
	 * @param playlist
	 * @return playlist content
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	@WebRequest(path = "playlist_content", description = "List items of specified playlist.", genericClass = BeanPlaylistItem.class)
	public ArrayList<BeanPlaylistItem> getPlaylistContent(@WebGet(name = "token") String token,
			@WebGet(name = "id") String id, @WebGet(name = "playlist") String playlist)
			throws RemoteException, PlayerException;

	/**
	 * Get files and directories at specific path.
	 *
	 * @param id
	 * @param path
	 * @return playing bean
	 * @throws RemoteException
	 */
	@WebRequest(path = "files", description = "Get files and directories at specific path.", genericClass = BeanFileSystem.class)
	public ArrayList<BeanFileSystem> getFiles(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "path", required = false, defaultvalue = "") String path) throws RemoteException;

	/**
	 * Play specified file or directory.
	 *
	 * @param id
	 * @param player
	 * @param file
	 * @return playing bean
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	@WebRequest(path = "play_file", description = "Play specified file or directory.")
	public PlayingBean playFile(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "player") String player, @WebGet(name = "file") String file)
			throws RemoteException, PlayerException;

	/**
	 * Play specified playlist.
	 *
	 * @param id
	 * @param player
	 * @param playlist
	 * @return playing bean
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	@WebRequest(path = "play_playlist", description = "Play specified playlist.")
	public PlayingBean playPlaylist(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "player") String player, @WebGet(name = "playlist") String playlist)
			throws RemoteException, PlayerException;

	/**
	 * Play youtube url.
	 *
	 * @param id
	 * @param player
	 * @param youtube_url
	 * @return playing bean
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	@WebRequest(path = "play_youtube", description = "Play youtube url.")
	public PlayingBean playYoutube(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "player") String player, @WebGet(name = "youtube_url") String youtube_url)
			throws RemoteException, PlayerException;

	/**
	 * Change state of player. Plaing -> Pause and Pause -> Playing. If player does
	 * not play anything, throw player exception.
	 *
	 * @param id
	 * @param player
	 * @return playing bean
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	@WebRequest(path = "play_pause", description = "Change state of player. Plaing -> Pause and Pause -> Playing. If player does not play anything, throw player exception.")
	public PlayingBean playPause(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "player") String player) throws RemoteException, PlayerException;

	/**
	 * Play next file in playlist or filesystem.
	 *
	 * @param id
	 * @param player
	 * @return playing bean
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	@WebRequest(path = "next", description = "Play next file in playlist or filesystem.")
	public PlayingBean playNext(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "player") String player) throws RemoteException, PlayerException;

	/**
	 * Play previous file in playlist or filesystem.
	 *
	 * @param id
	 * @param player
	 * @return playing bean
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	@WebRequest(path = "previous", description = "Play previous file in playlist or filesystem.")
	public PlayingBean playPrevious(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "player") String player) throws RemoteException, PlayerException;

	/**
	 * Seek forward in current playing.
	 *
	 * @param id
	 * @param player
	 * @return playing bean
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	@WebRequest(path = "seek_forward", description = "Seek forward in current playing.")
	public PlayingBean playSeekForward(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "player") String player) throws RemoteException, PlayerException;

	/**
	 * Seek backward in current playing.
	 *
	 * @param id
	 * @param player
	 * @return playing bean
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	@WebRequest(path = "seek_backward", description = "Seek backward in current playing.")
	public PlayingBean playSeekBackward(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "player") String player) throws RemoteException, PlayerException;

	/**
	 * Set the specified player in fullsceen mode or change to windowed mode.
	 *
	 * @param id
	 * @param player
	 * @return playing bean
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	@WebRequest(path = "set_fullscreen", description = "Set the specified player in fullsceen mode or change to windowed mode.")
	public PlayingBean playSetFullscreen(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "player") String player, @WebGet(name = "fullscreen") boolean fullscreen)
			throws RemoteException, PlayerException;

	/**
	 * Stop playing.
	 *
	 * @param id
	 * @param player
	 * @return playing bean
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	@WebRequest(path = "stop", description = "Stop playing.")
	public PlayingBean playStop(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "player") String player) throws RemoteException, PlayerException;

	/**
	 * Set volume of the player. Value must between 0 and 100.
	 *
	 * @param id
	 * @param player
	 * @param volume
	 * @return playing bean
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	@WebRequest(path = "volume", description = "Set volume of the player. Value must between 0 and 100.")
	public PlayingBean setVolume(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "player") String player, @WebGet(name = "volume") int volume)
			throws RemoteException, PlayerException;

	/**
	 * Change volume of the player by given delta.
	 *
	 * @param id
	 * @param player
	 * @param volume
	 * @return playing bean
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	@WebRequest(path = "delta_volume", description = "Change volume of the player by given delta.")
	public PlayingBean setDeltaVolume(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "player") String player, @WebGet(name = "delta") int delta)
			throws RemoteException, PlayerException;

	/**
	 * Publish specified file or directory for one download.
	 *
	 * @param id
	 * @param path
	 * @return BeanDownload
	 * @throws RemoteException
	 * @throws IOException
	 */
	@WebRequest(path = "publish_for_download", description = "Publish specified file or directory for one download.")
	public BeanDownload publishForDownload(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "path") String path) throws RemoteException, IOException;

	/**
	 * Create new media server
	 *
	 * @param token
	 * @param id
	 * @param name
	 * @param locationBrowser
	 * @param locationPlaylist
	 * @param description
	 * @param type
	 * @param x
	 * @param y
	 * @param z
	 * @return new media server
	 * @throws RemoteException
	 * @throws IOException
	 * @throws DaoException
	 */
	@WebRequest(path = "create", description = "Create new media server.")
	public BeanMediaServer createNewMediaServer(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "name") String name, @WebGet(name = "location_browser") String locationBrowser,
			@WebGet(name = "location_playlist") String locationPlaylist,
			@WebGet(name = "description") String description, @WebGet(name = "x") float x, @WebGet(name = "y") float y,
			@WebGet(name = "z") float z) throws RemoteException, IOException, DaoException;

	/**
	 * Update existing media server
	 *
	 * @param token
	 * @param id
	 * @param name
	 * @param locationBrowser
	 * @param locationPlaylist
	 * @param description
	 * @param x
	 * @param y
	 * @param z
	 * @return updated media server
	 * @throws RemoteException
	 * @throws IOException
	 * @throws DaoException
	 */
	@WebRequest(path = "update", description = "Update existing media server.")
	public BeanMediaServer updateExistingMediaServer(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "name") String name, @WebGet(name = "location_browser") String locationBrowser,
			@WebGet(name = "location_playlist") String locationPlaylist,
			@WebGet(name = "description") String description, @WebGet(name = "x") float x, @WebGet(name = "y") float y,
			@WebGet(name = "z") float z) throws RemoteException, IOException, DaoException;

	/**
	 * Delete media server
	 *
	 * @param token
	 * @param id
	 * @throws RemoteException
	 * @throws IOException
	 * @throws DaoException
	 */
	@WebRequest(path = "delete", description = "Delete media server.")
	public void deleteMediaServer(@WebGet(name = "token") String token, @WebGet(name = "id") String id)
			throws RemoteException, IOException, DaoException;

	public static class BeanFileSystem implements Comparable<BeanFileSystem> {

		@WebField(name = "filetype")
		private FileType mFileType;

		@WebField(name = "name")
		private String mName;

		public FileType getFileType() {
			return mFileType;
		}

		public void setFileType(FileType fileType) {
			mFileType = fileType;
		}

		public String getName() {
			return mName;
		}

		public void setName(String name) {
			mName = name;
		}

		@Override
		public int compareTo(BeanFileSystem another) {
			if (another == null)
				return 0;
			if (getFileType() == another.getFileType())
				return mName.compareToIgnoreCase(another.getName());
			if (getFileType() == FileType.Directory)
				return -1;
			return 1;
		}
	}

	public static class BeanPlaylist implements Comparable<BeanPlaylist> {

		@WebField(name = "name")
		private String mName;

		@WebField(name = "item_count")
		private int mItemCount;

		public String getName() {
			return mName;
		}

		public void setName(String name) {
			mName = name;
		}

		public int getItemCount() {
			return mItemCount;
		}

		public void setItemCount(int itemCount) {
			mItemCount = itemCount;
		}

		@Override
		public int compareTo(BeanPlaylist another) {
			return mName.compareToIgnoreCase(another.mName);
		}

	}

	public static class BeanPlaylistItem {

		@WebField(name = "name")
		private String mName;

		@WebField(name = "path")
		private String mPath;

		public String getName() {
			return mName;
		}

		public void setName(String name) {
			mName = name;
		}

		public String getPath() {
			return mPath;
		}

		public void setPath(String path) {
			mPath = path;
		}

	}

	public class BeanDownload implements Serializable {
		public enum DownloadType {
			File, Directory
		};

		@WebField(name = "ip")
		private String mIP;
		@WebField(name = "port")
		private int mPort;
		@WebField(name = "type")
		private DownloadType mType;

		public String getIP() {
			return mIP;
		}

		public void setIP(String iP) {
			mIP = iP;
		}

		public int getPort() {
			return mPort;
		}

		public void setPort(int port) {
			mPort = port;
		}

		public DownloadType getType() {
			return mType;
		}

		public void setType(DownloadType type) {
			mType = type;
		}
	}

	public class BeanMediaServer extends BeanWeb {

		@WebField(name = "current_playing")
		private PlayingBean mCurrentPlaying;

		public PlayingBean getCurrentPlaying() {
			return mCurrentPlaying;
		}

		public void setCurrentPlaying(PlayingBean currentPlaying) {
			mCurrentPlaying = currentPlaying;
		}

	}

}
