package de.neo.remote.mediaserver;

import java.util.ArrayList;

import de.neo.remote.AbstractUnitHandler;
import de.neo.remote.api.IControlCenter;
import de.neo.remote.api.IControlUnit;
import de.neo.remote.api.IMediaServer;
import de.neo.remote.api.IWebMediaServer;
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
	@WebRequest(path = "playlists", description = "List all playlists of specified media server.", genericClass = BeanMediaServer.class)
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

	@Override
	public String getWebPath() {
		return "mediaserver";
	}

}
