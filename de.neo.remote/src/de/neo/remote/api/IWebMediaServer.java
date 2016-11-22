package de.neo.remote.api;

import java.util.List;

import de.neo.remote.api.IControlCenter.BeanWeb;
import de.neo.rmi.api.WebField;
import de.neo.rmi.api.WebGet;
import de.neo.rmi.api.WebRequest;
import de.neo.rmi.protokol.RemoteAble;

public interface IWebMediaServer extends RemoteAble {

	/**
	 * List all registered media-server with current playing state. Optional
	 * parameter id specified required media-server.
	 * 
	 * @param id
	 * @return media-server list
	 */
	@WebRequest(path = "list", description = "List all registered media-server with current playing state. Optional parameter id specified required media-server.")
	public List<BeanMediaServer> getMediaServer(@WebGet(name = "id", required = false, defaultvalue = "") String id);

	/**
	 * List all playlists of specified media server.
	 * 
	 * @param id
	 * @return playlists
	 */
	@WebRequest(path = "playlists", description = "List all playlists of specified media server.")
	public List<BeanPlaylist> getPlaylists(@WebGet(name = "id") String id);

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
