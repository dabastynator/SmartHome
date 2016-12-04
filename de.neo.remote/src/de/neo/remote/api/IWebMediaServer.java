package de.neo.remote.api;

import java.util.ArrayList;

import de.neo.remote.api.IControlCenter.BeanWeb;
import de.neo.rmi.api.WebField;
import de.neo.rmi.api.WebGet;
import de.neo.rmi.api.WebRequest;
import de.neo.rmi.protokol.RemoteAble;
import de.neo.rmi.protokol.RemoteException;

public interface IWebMediaServer extends RemoteAble {

	public enum FileType {
		Directory, File
	}

	public static final String FileSeparator = "<->";

	/**
	 * List all registered media-server with current playing state. Optional
	 * parameter id specified required media-server.
	 * 
	 * @param id
	 * @return media-server list
	 */
	@WebRequest(path = "list", description = "List all registered media-server with current playing state. Optional parameter id specified required media-server.", genericClass = BeanMediaServer.class)
	public ArrayList<BeanMediaServer> getMediaServer(
			@WebGet(name = "id", required = false, defaultvalue = "") String id) throws RemoteException;

	/**
	 * List all playlists of specified media server.
	 * 
	 * @param id
	 * @return playlists
	 */
	@WebRequest(path = "playlists", description = "List all playlists of specified media server.", genericClass = BeanMediaServer.class)
	public ArrayList<BeanPlaylist> getPlaylists(@WebGet(name = "id") String id) throws RemoteException;

	/**
	 * Get files and directories at specific path.
	 * 
	 * @param id
	 * @param path
	 * @return playing bean
	 * @throws RemoteException
	 */
	@WebRequest(path = "files", description = "Get files and directories at specific path.")
	public ArrayList<BeanFileSystem> getFiles(@WebGet(name = "id") String id,
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
	public PlayingBean playFile(@WebGet(name = "id") String id, @WebGet(name = "player") String player,
			@WebGet(name = "file") String file) throws RemoteException, PlayerException;

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
	public PlayingBean playPlaylist(@WebGet(name = "id") String id, @WebGet(name = "player") String player,
			@WebGet(name = "playlist") String playlist) throws RemoteException, PlayerException;

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
	public PlayingBean playYoutube(@WebGet(name = "id") String id, @WebGet(name = "player") String player,
			@WebGet(name = "youtube_url") String youtube_url) throws RemoteException, PlayerException;

	/**
	 * Change state of player. Plaing -> Pause and Pause -> Playing. If player
	 * does not play anything, throw player exception.
	 * 
	 * @param id
	 * @param player
	 * @return playing bean
	 * @throws RemoteException
	 * @throws PlayerException
	 */
	@WebRequest(path = "play_pause", description = "Change state of player. Plaing -> Pause and Pause -> Playing. If player does not play anything, throw player exception.")
	public PlayingBean playPause(@WebGet(name = "id") String id, @WebGet(name = "player") String player)
			throws RemoteException, PlayerException;

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
	public PlayingBean playNext(@WebGet(name = "id") String id, @WebGet(name = "player") String player)
			throws RemoteException, PlayerException;

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
	public PlayingBean playPrevious(@WebGet(name = "id") String id, @WebGet(name = "player") String player)
			throws RemoteException, PlayerException;

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
	public PlayingBean playSeekForward(@WebGet(name = "id") String id, @WebGet(name = "player") String player)
			throws RemoteException, PlayerException;

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
	public PlayingBean playSeekBackward(@WebGet(name = "id") String id, @WebGet(name = "player") String player)
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
	public PlayingBean playStop(@WebGet(name = "id") String id, @WebGet(name = "player") String player)
			throws RemoteException, PlayerException;

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
	public PlayingBean setVolume(@WebGet(name = "id") String id, @WebGet(name = "player") String player,
			@WebGet(name = "volume") int volume) throws RemoteException, PlayerException;

	public static class BeanFileSystem {

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
	}

	public static class BeanPlaylist {

		@WebField(name = "name")
		private String mName;

		public String getName() {
			return mName;
		}

		public void setName(String name) {
			mName = name;
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
