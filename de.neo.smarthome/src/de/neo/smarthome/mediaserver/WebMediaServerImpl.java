package de.neo.smarthome.mediaserver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.neo.persist.Dao;
import de.neo.persist.DaoException;
import de.neo.persist.DaoFactory;
import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebParam;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.AbstractUnitHandler;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.SmartHome.ControlUnitFactory;
import de.neo.smarthome.api.IControllUnit;
import de.neo.smarthome.api.IWebMediaServer;
import de.neo.smarthome.api.PlayerException;
import de.neo.smarthome.api.PlayingBean;
import de.neo.smarthome.api.PlayingBean.STATE;
import de.neo.smarthome.controlcenter.ControlCenter;
import de.neo.smarthome.user.User;
import de.neo.smarthome.user.User.UserRole;
import de.neo.smarthome.user.UserSessionHandler;

public class WebMediaServerImpl extends AbstractUnitHandler implements IWebMediaServer
{
	private class FileCache
	{
		static int MAX_SIZE = 5;
		
		private class CacheEntry
		{
			String mPath;
			ArrayList<BeanFileSystem> mFiles = new ArrayList<>();
		}
		
		private List<CacheEntry> mEntries = new ArrayList<>();
		
		public ArrayList<BeanFileSystem> getCachedFiles(String path)
		{
			int idx = -1;
			for (int i = 0; i < mEntries.size(); i++)
			{
				if (mEntries.get(i).mPath.equals(path))
				{
					idx = i;
					break;
				}
			}
			if (idx >= 0)
			{
				CacheEntry entry = mEntries.get(idx);
				mEntries.remove(idx);
				mEntries.add(0, entry);
				return entry.mFiles;
			}
			return null;
		}
		
		public void addCachedFiles(String path, ArrayList<BeanFileSystem> files)
		{
			CacheEntry entry = new CacheEntry();
			entry.mPath = path;
			entry.mFiles = files;
			while (mEntries.size() >= MAX_SIZE)
			{
				mEntries.remove(mEntries.size()-1);
			}
			mEntries.add(0, entry);
		}
	}
	
	private Map<User, FileCache> mFilesCache = new HashMap<>();

	public WebMediaServerImpl(ControlCenter center)
	{
		super(center);
	}

	@Override
	@WebRequest(path = "list", description = "List all registered media-server with current playing state. Optional parameter id specified required media-server.", genericClass = BeanMediaServer.class)
	public ArrayList<BeanMediaServer> getMediaServer(
			@WebParam(name = "token") String token,
			@WebParam(name = "id", required = false, defaultvalue = "") String id) throws RemoteException
	{
		User user = UserSessionHandler.require(token);
		ArrayList<BeanMediaServer> result = new ArrayList<>();
		if (id != null && id.length() > 0)
		{
			IControllUnit unit = mCenter.getAccessHandler().require(user, id);
			if (unit instanceof MediaControlUnit)
			{
				MediaControlUnit mediaServer = (MediaControlUnit) unit;
				BeanMediaServer webMedia = getBeanFor(unit, mediaServer);
				result.add(webMedia);
			}
			else
				throw new RemoteException("No such mediaserver found: " + id);
		}
		else
		{
			for (IControllUnit unit : mCenter.getAccessHandler().unitsFor(user))
			{
				try
				{
					if (unit instanceof MediaControlUnit)
					{
						MediaControlUnit mediaServer = (MediaControlUnit) unit;
						BeanMediaServer webMedia = getBeanFor(unit, mediaServer);
						result.add(webMedia);
					}
				}
				catch (RemoteException e) 
				{
				}
			}
		}
		return result;
	}

	private BeanMediaServer getBeanFor(IControllUnit unit, MediaControlUnit mediaServer) throws RemoteException
	{
		BeanMediaServer webMedia = new BeanMediaServer();
		webMedia.merge(unit.getWebBean());
		webMedia.mID = unit.getID();
		if (mediaServer.getMPlayer().getPlayingBean() != null
				&& mediaServer.getMPlayer().getPlayingBean().mState != STATE.DOWN)
			webMedia.currentPlaying = mediaServer.getMPlayer().getPlayingBean();
		if (mediaServer.getOMXPlayer().getPlayingBean() != null
				&& mediaServer.getOMXPlayer().getPlayingBean().mState != STATE.DOWN)
			webMedia.currentPlaying = mediaServer.getOMXPlayer().getPlayingBean();
		if (mediaServer.getTotemPlayer().getPlayingBean() != null
				&& mediaServer.getTotemPlayer().getPlayingBean().mState != STATE.DOWN)
			webMedia.currentPlaying = mediaServer.getTotemPlayer().getPlayingBean();
		return webMedia;
	}

	@Override
	@WebRequest(path = "playlists", description = "List all playlists of specified media server.", genericClass = BeanPlaylist.class)
	public ArrayList<BeanPlaylist> getPlaylists(
			@WebParam(name = "token") String token, 
			@WebParam(name = "id") String id)
			throws RemoteException
	{
		ArrayList<BeanPlaylist> result = new ArrayList<>();
		MediaControlUnit mediaServer = mCenter.getAccessHandler().require(token, id);
		String[] playlists = mediaServer.getPlayList().getPlayLists();
		Arrays.sort(playlists);
		for (String str : playlists)
		{
			BeanPlaylist pls = new BeanPlaylist();
			pls.name = str;
			result.add(pls);
		}
		return result;
	}

	@WebRequest(path = "playlist_content", description = "List items of specified playlist.", genericClass = BeanPlaylistItem.class)
	public ArrayList<BeanPlaylistItem> getPlaylistContent(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "playlist") String playlist)
			throws RemoteException, PlayerException
	{
		ArrayList<BeanPlaylistItem> result = new ArrayList<>();
		MediaControlUnit mediaServer = mCenter.getAccessHandler().require(token, id);
		String path = mediaServer.getBrowserPath();
		for (String str : mediaServer.getPlayList().listContent(playlist))
		{
			BeanPlaylistItem pls = new BeanPlaylistItem();
			pls.path = str;
			if (str.indexOf("/") >= 0)
			{
				pls.name = str.substring(str.lastIndexOf("/") + 1);
				if (str.startsWith(path))
					pls.path = str.substring(path.length());
			}
			else
			{
				pls.name = str;
			}
			result.add(pls);
		}
		return result;
	}

	@WebRequest(path = "playlist_extend", description = "Add item to specified playlist.")
	public void playlistExtend(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "playlist") String playlist,
			@WebParam(name = "item") String item) throws RemoteException, PlayerException
	{
		MediaControlUnit mediaServer = mCenter.getAccessHandler().require(token, id);
		if (!item.startsWith(mediaServer.getBrowserPath()))
			item = mediaServer.getBrowserPath() + item;
		mediaServer.getPlayList().extendPlayList(playlist, item);
	}

	@WebRequest(path = "playlist_create", description = "Create new playlist.")
	public void playlistCreate(
			@WebParam(name = "token") String token, 
			@WebParam(name = "id") String id,
			@WebParam(name = "playlist") String playlist) throws RemoteException
	{
		MediaControlUnit mediaServer = mCenter.getAccessHandler().require(token, id);
		mediaServer.getPlayList().addPlayList(playlist);
	}

	@WebRequest(path = "playlist_delete", description = "Delete specified playlist.")
	public void playlistDelete(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "playlist") String playlist) throws RemoteException, PlayerException
	{
		MediaControlUnit mediaServer = mCenter.getAccessHandler().require(token, id);
		mediaServer.getPlayList().removePlayList(playlist);
	}

	@WebRequest(path = "playlist_delete_item", description = "Delete item from specified playlist.")
	public void playlistDeleteItem(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "playlist") String playlist,
			@WebParam(name = "item") String item)
			throws RemoteException, PlayerException
	{
		MediaControlUnit mediaServer = mCenter.getAccessHandler().require(token, id);
		mediaServer.getPlayList().removeItem(playlist, item);
	}

	@Override
	public String getWebPath()
	{
		return "mediaserver";
	}

	@WebRequest(path = "files", description = "Get files and directories at specific path.", genericClass = BeanFileSystem.class)
	public ArrayList<BeanFileSystem> getFiles(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "path", required = false, defaultvalue = "") String path)
					throws RemoteException
	{
		if (!path.endsWith(File.separator))
		{
			path += File.separator;
		}
		User user = UserSessionHandler.require(token);
		FileCache cache;
		if (mFilesCache.containsKey(user))
		{
			cache = mFilesCache.get(user);
		}
		else
		{
			cache = new FileCache();
			mFilesCache.put(user, cache);
		}
		ArrayList<BeanFileSystem> result = cache.getCachedFiles(path);
		if (result != null)
		{
			return result;
		}
		result = new ArrayList<>();	
		MediaControlUnit mediaServer = mCenter.getAccessHandler().require(token, id);
		for (String dir : mediaServer.listDirectories(path))
		{
			BeanFileSystem bean = new BeanFileSystem();
			bean.name = dir;
			bean.path = path + dir;
			bean.fileType = FileType.Directory;
			result.add(bean);
		}
		for (String file : mediaServer.listFiles(path))
		{
			BeanFileSystem bean = new BeanFileSystem();
			bean.name = file;
			bean.path = path + file;
			bean.fileType = FileType.File;
			result.add(bean);
		}
		cache.addCachedFiles(path, result);
		return result;
	}
	
	@WebRequest(path = "search", description = "Search for files and directories at specific path.", genericClass = BeanFileSystem.class)
	public ArrayList<BeanFileSystem> searchFiles(
			@WebParam(name = "token") String token, 
			@WebParam(name = "id") String id,
			@WebParam(name = "target") String target,
			@WebParam(name = "path", required = false, defaultvalue = "") String path)
					throws RemoteException
	{
		if (!path.endsWith(File.separator))
		{
			path += File.separator;
		}
		MediaControlUnit mediaServer = mCenter.getAccessHandler().require(token, id);
		return mediaServer.search(path, target);
	}

	private IPlayer getPlayer(String token, String id, String player) throws PlayerException, RemoteException
	{
		MediaControlUnit mediaServer = mCenter.getAccessHandler().require(token, id);
		if ("mplayer".equals(player))
			return mediaServer.getMPlayer();
		else if ("omxplayer".equals(player))
			return mediaServer.getOMXPlayer();
		else if ("totem".equals(player))
			return mediaServer.getTotemPlayer();
		else
			throw new PlayerException("Unknown player: " + player);
	}

	@WebRequest(path = "play_file", description = "Play specified file or directory.")
	public PlayingBean playFile(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "player") String player,
			@WebParam(name = "file") String file)
			throws PlayerException, RemoteException
	{
		IPlayer p = getPlayer(token, id, player);
		MediaControlUnit mediaServer = mCenter.getAccessHandler().require(token, id);
		file = mediaServer.getBrowserPath() + file;
		p.play(file);
		return p.getPlayingBean();
	}

	@WebRequest(path = "play_playlist", description = "Play specified playlist.")
	public PlayingBean playPlaylist(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "player") String player,
			@WebParam(name = "playlist") String playlist)
			throws RemoteException, PlayerException
	{
		IPlayer p = getPlayer(token, id, player);
		MediaControlUnit mediaServer = mCenter.getAccessHandler().require(token, id);
		String path = mediaServer.getPlayList().getPlaylistFullpath(playlist);
		p.playPlayList(path);
		return p.getPlayingBean();
	}

	@WebRequest(path = "play_pause", description = "Change state of player. Plaing -> Pause and Pause -> Playing. If player does not play anything, throw player exception.")
	public PlayingBean playPause(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "player") String player) 
					throws RemoteException, PlayerException
	{
		IPlayer p = getPlayer(token, id, player);
		if (p != null)
		{
			p.playPause();
			return p.getPlayingBean();
		}
		return null;
	}

	@WebRequest(path = "next", description = "Play next file in playlist or filesystem.")
	public PlayingBean playNext(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "player") String player) 
					throws RemoteException, PlayerException
	{
		IPlayer p = getPlayer(token, id, player);
		if (p != null)
		{
			p.next();
			return p.getPlayingBean();
		}
		return null;
	}

	@WebRequest(path = "previous", description = "Play previous file in playlist or filesystem.")
	public PlayingBean playPrevious(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "player") String player)
					throws RemoteException, PlayerException
	{
		IPlayer p = getPlayer(token, id, player);
		if (p != null)
		{
			p.previous();
			return p.getPlayingBean();
		}
		return null;
	}

	@WebRequest(path = "seek_forward", description = "Seek forward in current playing.")
	public PlayingBean playSeekForward(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "player") String player)
					throws RemoteException, PlayerException
	{
		IPlayer p = getPlayer(token, id, player);
		if (p != null)
		{
			p.seekForwards();
			return p.getPlayingBean();
		}
		return null;
	}

	@WebRequest(path = "seek_backward", description = "Seek backward in current playing.")
	public PlayingBean playSeekBackward(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "player") String player)
					throws RemoteException, PlayerException
	{
		IPlayer p = getPlayer(token, id, player);
		if (p != null)
		{
			p.seekBackwards();
			return p.getPlayingBean();
		}
		return null;
	}

	@WebRequest(path = "seek", description = "Seek to specific time in current playing.")
	public PlayingBean playSeek(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "player") String player,
			@WebParam(name = "seek_time_sec") int timeSec)
					throws RemoteException, PlayerException
	{
		IPlayer p = getPlayer(token, id, player);
		if (p != null)
		{
			p.setPlayingPosition(timeSec);
			return p.getPlayingBean();
		}
		return null;
	}


	@WebRequest(path = "stop", description = "Stop playing.")
	public PlayingBean playStop(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "player") String player)
					throws RemoteException, PlayerException
	{
		IPlayer p = getPlayer(token, id, player);
		if (p != null)
		{
			p.quit();
			return p.getPlayingBean();
		}
		return null;
	}

	@WebRequest(path = "volume", description = "Set volume of the player. Value must between 0 and 100.")
	public PlayingBean setVolume(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "player") String player,
			@WebParam(name = "volume") int volume)
			throws RemoteException, PlayerException
	{
		IPlayer p = getPlayer(token, id, player);
		if (p != null)
		{
			p.setVolume(Math.max(0, Math.min(100, volume)));
			return p.getPlayingBean();
		}
		return null;
	}

	@WebRequest(path = "delta_volume", description = "Change volume of the player by given delta.")
	public PlayingBean setDeltaVolume(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "player") String player,
			@WebParam(name = "delta") int delta)
			throws RemoteException, PlayerException
	{
		IPlayer p = getPlayer(token, id, player);
		if (p != null)
		{
			p.setVolume(Math.max(0, Math.min(100, p.getVolume() + delta)));
			return p.getPlayingBean();
		}
		return null;
	}

	@WebRequest(path = "set_fullscreen", description = "Set the specified player in fullsceen mode or change to windowed mode.")
	public PlayingBean playSetFullscreen(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "player") String player,
			@WebParam(name = "fullscreen") boolean fullscreen)
			throws RemoteException, PlayerException
	{
		IPlayer p = getPlayer(token, id, player);
		if (p != null)
		{
			p.fullScreen(fullscreen);
			return p.getPlayingBean();
		}
		return null;
	}

	@WebRequest(path = "create", description = "Create new media server.")
	public BeanMediaServer createNewMediaServer(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "name") String name,
			@WebParam(name = "location_browser") String locationBrowser,
			@WebParam(name = "location_playlist") String locationPlaylist,
			@WebParam(name = "description") String description)
					throws RemoteException, IOException, DaoException
	{
		UserSessionHandler.require(token, UserRole.ADMIN);
		MediaControlUnit unit = new MediaControlUnit();
		unit.setId(id);
		unit.setName(name);
		unit.setBrowserLocation(locationBrowser);
		unit.setPlaylistLocation(locationPlaylist);
		Dao<MediaControlUnit> dao = DaoFactory.getInstance().getDao(MediaControlUnit.class);
		dao.save(unit);
		mCenter.addControlUnit(unit);
		RemoteLogger.performLog(LogPriority.INFORMATION, "Create new media server " + unit.getName(), "WebMediaServer");
		return getBeanFor(unit, unit);
	}

	@WebRequest(path = "update", description = "Update existing media server.")
	public BeanMediaServer updateExistingMediaServer(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id,
			@WebParam(name = "name") String name,
			@WebParam(name = "location_browser") String locationBrowser,
			@WebParam(name = "location_playlist") String locationPlaylist,
			@WebParam(name = "description") String description)
					throws RemoteException, IOException, DaoException
	{
		UserSessionHandler.require(token, UserRole.ADMIN);
		IControllUnit u = mCenter.getControlUnit(id);
		if (!(u instanceof MediaControlUnit))
		{
			throw new RemoteException("Unknown media server " + id);
		}
		MediaControlUnit unit = (MediaControlUnit) u;
		unit.setId(id);
		unit.setName(name);
		unit.setBrowserLocation(locationBrowser);
		unit.setPlaylistLocation(locationPlaylist);
		Dao<MediaControlUnit> dao = DaoFactory.getInstance().getDao(MediaControlUnit.class);
		dao.update(unit);
		mCenter.addControlUnit(unit);
		RemoteLogger.performLog(LogPriority.INFORMATION, "Create new media server " + unit.getName(), "WebMediaServer");
		return getBeanFor(unit, unit);
	}

	@WebRequest(path = "delete", description = "Delete media server.")
	public void deleteMediaServer(
			@WebParam(name = "token") String token,
			@WebParam(name = "id") String id)
			throws RemoteException, IOException, DaoException
	{
		UserSessionHandler.require(token, UserRole.ADMIN);
		IControllUnit unit = mCenter.getControlUnit(id);
		if (unit instanceof MediaControlUnit)
		{
			MediaControlUnit mediaServer = (MediaControlUnit) unit;
			Dao<MediaControlUnit> dao = DaoFactory.getInstance().getDao(MediaControlUnit.class);
			dao.delete(mediaServer);
			RemoteLogger.performLog(LogPriority.INFORMATION, "Delete media server " + mediaServer.getName(),
					"WebMediaServer");
		}
	}

	public static class MediaFactory implements ControlUnitFactory
	{

		@Override
		public Class<?> getUnitClass()
		{
			return MediaControlUnit.class;
		}

		@Override
		public AbstractUnitHandler createUnitHandler(ControlCenter center)
		{
			return new WebMediaServerImpl(center);
		}

	}

}
