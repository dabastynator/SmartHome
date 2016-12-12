package de.neo.remote.mediaserver;

import java.io.File;
import java.util.ArrayList;

import de.neo.remote.AbstractUnitHandler;
import de.neo.remote.api.IControlCenter;
import de.neo.remote.api.IControlUnit;
import de.neo.remote.api.IMediaServer;
import de.neo.remote.api.IPlayer;
import de.neo.remote.api.IWebMediaServer;
import de.neo.remote.api.PlayerException;
import de.neo.remote.api.PlayingBean;
import de.neo.remote.api.PlayingBean.STATE;
import de.neo.rmi.api.WebGet;
import de.neo.rmi.api.WebRequest;
import de.neo.rmi.protokol.RemoteException;

public class WebMediaServerImpl extends AbstractUnitHandler implements IWebMediaServer {

	public WebMediaServerImpl(IControlCenter center) {
		super(center);
	}

	@Override
	@WebRequest(path = "list", description = "List all registered media-server with current playing state. Optional parameter id specified required media-server.", genericClass = BeanMediaServer.class)
	public ArrayList<BeanMediaServer> getMediaServer(
			@WebGet(name = "id", required = false, defaultvalue = "") String id) {
		ArrayList<BeanMediaServer> result = new ArrayList<>();
		for (IControlUnit unit : mCenter.getControlUnits().values()) {
			try {
				if (unit.getRemoteableControlObject() instanceof IMediaServer) {
					IMediaServer mediaServer = (IMediaServer) unit.getRemoteableControlObject();
					BeanMediaServer webMedia = new BeanMediaServer();
					unit.config(webMedia);
					if (id.equals(unit.getID()) || id.length() == 0) {
						webMedia.setID(unit.getID());
						if (mediaServer.getMPlayer().getPlayingBean() != null
								&& mediaServer.getMPlayer().getPlayingBean().getState() != STATE.DOWN)
							webMedia.setCurrentPlaying(mediaServer.getMPlayer().getPlayingBean());
						if (mediaServer.getOMXPlayer().getPlayingBean() != null
								&& mediaServer.getOMXPlayer().getPlayingBean().getState() != STATE.DOWN)
							webMedia.setCurrentPlaying(mediaServer.getOMXPlayer().getPlayingBean());
						if (mediaServer.getTotemPlayer().getPlayingBean() != null
								&& mediaServer.getTotemPlayer().getPlayingBean().getState() != STATE.DOWN)
							webMedia.setCurrentPlaying(mediaServer.getTotemPlayer().getPlayingBean());
						result.add(webMedia);
					}
				}
			} catch (RemoteException e) {
			}
		}
		return result;
	}

	@Override
	@WebRequest(path = "playlists", description = "List all playlists of specified media server.", genericClass = BeanPlaylist.class)
	public ArrayList<BeanPlaylist> getPlaylists(@WebGet(name = "id") String id) {
		ArrayList<BeanPlaylist> result = new ArrayList<>();
		try {
			IControlUnit unit = mCenter.getControlUnit(id);
			if (unit.getRemoteableControlObject() instanceof IMediaServer) {
				IMediaServer mediaServer = (IMediaServer) unit.getRemoteableControlObject();
				for (String str : mediaServer.getPlayList().getPlayLists()) {
					BeanPlaylist pls = new BeanPlaylist();
					pls.setName(str);
					result.add(pls);
				}
			}
		} catch (RemoteException e) {
		}
		return result;
	}

	@WebRequest(path = "playlist_content", description = "List items of specified playlist.", genericClass = BeanPlaylistItem.class)
	public ArrayList<BeanPlaylistItem> getPlaylistContent(@WebGet(name = "id") String id,
			@WebGet(name = "playlist") String playlist) throws RemoteException, PlayerException {
		ArrayList<BeanPlaylistItem> result = new ArrayList<>();
		try {
			IControlUnit unit = mCenter.getControlUnit(id);
			if (unit.getRemoteableControlObject() instanceof IMediaServer) {
				IMediaServer mediaServer = (IMediaServer) unit.getRemoteableControlObject();
				for (String str : mediaServer.getPlayList().listContent(playlist)) {
					BeanPlaylistItem pls = new BeanPlaylistItem();
					pls.setPath(str);
					if (str.indexOf("/") >= 0)
						pls.setName(str.substring(str.lastIndexOf("/") + 1));
					else
						pls.setName(str);
					result.add(pls);
				}
			}
		} catch (RemoteException e) {
		}
		return result;
	}

	@WebRequest(path = "playlist_extend", description = "Add item to specified playlist.")
	public void playlistExtend(@WebGet(name = "id") String id, @WebGet(name = "playlist") String playlist,
			@WebGet(name = "item") String item) throws RemoteException, PlayerException {
		IControlUnit unit = mCenter.getControlUnit(id);
		if (unit.getRemoteableControlObject() instanceof IMediaServer) {
			IMediaServer mediaServer = (IMediaServer) unit.getRemoteableControlObject();
			mediaServer.getPlayList().extendPlayList(playlist, item);
		}
	}

	@WebRequest(path = "playlist_create", description = "Create new playlist.")
	public void playlistCreate(@WebGet(name = "id") String id, @WebGet(name = "playlist") String playlist)
			throws RemoteException {
		IControlUnit unit = mCenter.getControlUnit(id);
		if (unit.getRemoteableControlObject() instanceof IMediaServer) {
			IMediaServer mediaServer = (IMediaServer) unit.getRemoteableControlObject();
			mediaServer.getPlayList().addPlayList(playlist);
		}
	}

	@WebRequest(path = "playlist_delete", description = "Delete specified playlist.")
	public void playlistDelete(@WebGet(name = "id") String id, @WebGet(name = "playlist") String playlist)
			throws RemoteException, PlayerException {
		IControlUnit unit = mCenter.getControlUnit(id);
		if (unit.getRemoteableControlObject() instanceof IMediaServer) {
			IMediaServer mediaServer = (IMediaServer) unit.getRemoteableControlObject();
			mediaServer.getPlayList().removePlayList(playlist);
		}
	}

	@WebRequest(path = "playlist_delete_item", description = "Delete item from specified playlist.")
	public void playlistDeleteItem(@WebGet(name = "id") String id, @WebGet(name = "playlist") String playlist,
			@WebGet(name = "item") String item) throws RemoteException, PlayerException {
		IControlUnit unit = mCenter.getControlUnit(id);
		if (unit.getRemoteableControlObject() instanceof IMediaServer) {
			IMediaServer mediaServer = (IMediaServer) unit.getRemoteableControlObject();
			mediaServer.getPlayList().removeItem(playlist, item);
		}
	}

	@Override
	public String getWebPath() {
		return "mediaserver";
	}

	@WebRequest(path = "files", description = "Get files and directories at specific path.", genericClass = BeanFileSystem.class)
	public ArrayList<BeanFileSystem> getFiles(@WebGet(name = "id") String id,
			@WebGet(name = "path", required = false, defaultvalue = "") String path) {
		ArrayList<BeanFileSystem> result = new ArrayList<>();
		try {
			IControlUnit unit = mCenter.getControlUnit(id);
			if (unit.getRemoteableControlObject() instanceof IMediaServer) {
				path = path.replace(IWebMediaServer.FileSeparator, File.separator);
				IMediaServer mediaServer = (IMediaServer) unit.getRemoteableControlObject();
				for (String dir : mediaServer.listDirectories(path)) {
					BeanFileSystem bean = new BeanFileSystem();
					bean.setName(dir);
					bean.setFileType(FileType.Directory);
					result.add(bean);
				}
				for (String dir : mediaServer.listFiles(path)) {
					BeanFileSystem bean = new BeanFileSystem();
					bean.setName(dir);
					bean.setFileType(FileType.File);
					result.add(bean);
				}
			}
		} catch (RemoteException e) {
		}
		return result;
	}

	private IPlayer getPlayer(String id, String player) throws PlayerException {
		try {
			IControlUnit unit = mCenter.getControlUnit(id);
			if (unit != null && unit.getRemoteableControlObject() instanceof IMediaServer) {
				IMediaServer mediaServer = (IMediaServer) unit.getRemoteableControlObject();
				if ("mplayer".equals(player))
					return mediaServer.getMPlayer();
				else if ("omxplayer".equals(player))
					return mediaServer.getOMXPlayer();
				else if ("totem".equals(player))
					return mediaServer.getTotemPlayer();
				else
					throw new PlayerException("Unknown player: " + player);
			}
		} catch (RemoteException e) {
		}
		return null;
	}

	@WebRequest(path = "play_file", description = "Play specified file or directory.")
	public PlayingBean playFile(@WebGet(name = "id") String id, @WebGet(name = "player") String player,
			@WebGet(name = "file") String file) throws PlayerException, RemoteException {
		file = file.replace(IWebMediaServer.FileSeparator, File.separator);
		IPlayer p = getPlayer(id, player);
		IControlUnit unit = mCenter.getControlUnit(id);
		if (p != null && unit != null && unit.getRemoteableControlObject() instanceof IMediaServer) {
			IMediaServer mediaServer = (IMediaServer) unit.getRemoteableControlObject();
			file = mediaServer.getBrowserPath() + file;
			p.play(file);
			return p.getPlayingBean();
		}
		return null;
	}

	@WebRequest(path = "play_playlist", description = "Play specified playlist.")
	public PlayingBean playPlaylist(@WebGet(name = "id") String id, @WebGet(name = "player") String player,
			@WebGet(name = "playlist") String playlist) throws RemoteException, PlayerException {
		IPlayer p = getPlayer(id, player);
		IControlUnit unit = mCenter.getControlUnit(id);
		if (p != null && unit != null && unit.getRemoteableControlObject() instanceof IMediaServer) {
			IMediaServer mediaServer = (IMediaServer) unit.getRemoteableControlObject();
			String path = mediaServer.getPlayList().getPlaylistFullpath(playlist);
			p.playPlayList(path);
			return p.getPlayingBean();
		}
		return null;
	}

	@WebRequest(path = "play_youtube", description = "Play youtube url.")
	public PlayingBean playYoutube(@WebGet(name = "id") String id, @WebGet(name = "player") String player,
			@WebGet(name = "youtube_url") String youtube_url) throws RemoteException, PlayerException {
		IPlayer p = getPlayer(id, player);
		if (p != null) {
			p.playFromYoutube(youtube_url);
			return p.getPlayingBean();
		}
		return null;
	}

	@WebRequest(path = "play_pause", description = "Change state of player. Plaing -> Pause and Pause -> Playing. If player does not play anything, throw player exception.")
	public PlayingBean playPause(@WebGet(name = "id") String id, @WebGet(name = "player") String player)
			throws RemoteException, PlayerException {
		IPlayer p = getPlayer(id, player);
		if (p != null) {
			p.playPause();
			return p.getPlayingBean();
		}
		return null;
	}

	@WebRequest(path = "next", description = "Play next file in playlist or filesystem.")
	public PlayingBean playNext(@WebGet(name = "id") String id, @WebGet(name = "player") String player)
			throws RemoteException, PlayerException {
		IPlayer p = getPlayer(id, player);
		if (p != null) {
			p.next();
			return p.getPlayingBean();
		}
		return null;
	}

	@WebRequest(path = "previous", description = "Play previous file in playlist or filesystem.")
	public PlayingBean playPrevious(@WebGet(name = "id") String id, @WebGet(name = "player") String player)
			throws RemoteException, PlayerException {
		IPlayer p = getPlayer(id, player);
		if (p != null) {
			p.previous();
			return p.getPlayingBean();
		}
		return null;
	}

	@WebRequest(path = "seek_forward", description = "Seek forward in current playing.")
	public PlayingBean playSeekForward(@WebGet(name = "id") String id, @WebGet(name = "player") String player)
			throws RemoteException, PlayerException {
		IPlayer p = getPlayer(id, player);
		if (p != null) {
			p.seekForwards();
			return p.getPlayingBean();
		}
		return null;
	}

	@WebRequest(path = "seek_backward", description = "Seek backward in current playing.")
	public PlayingBean playSeekBackward(@WebGet(name = "id") String id, @WebGet(name = "player") String player)
			throws RemoteException, PlayerException {
		IPlayer p = getPlayer(id, player);
		if (p != null) {
			p.seekBackwards();
			return p.getPlayingBean();
		}
		return null;
	}

	@WebRequest(path = "stop", description = "Stop playing.")
	public PlayingBean playStop(@WebGet(name = "id") String id, @WebGet(name = "player") String player)
			throws RemoteException, PlayerException {
		IPlayer p = getPlayer(id, player);
		if (p != null) {
			p.quit();
			return p.getPlayingBean();
		}
		return null;
	}

	@WebRequest(path = "volume", description = "Set volume of the player. Value must between 0 and 100.")
	public PlayingBean setVolume(@WebGet(name = "id") String id, @WebGet(name = "player") String player,
			@WebGet(name = "volume") int volume) throws RemoteException, PlayerException {
		IPlayer p = getPlayer(id, player);
		if (p != null) {
			p.setVolume(volume);
			return p.getPlayingBean();
		}
		return null;
	}

	@WebRequest(path = "set_fullscreen", description = "Set the specified player in fullsceen mode or change to windowed mode.")
	public PlayingBean playSetFullscreen(@WebGet(name = "id") String id, @WebGet(name = "player") String player,
			@WebGet(name = "fullscreen") boolean fullscreen) throws RemoteException, PlayerException {
		IPlayer p = getPlayer(id, player);
		if (p != null) {
			p.fullScreen(fullscreen);
			return p.getPlayingBean();
		}
		return null;
	}

}
