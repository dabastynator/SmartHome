package de.neo.smarthome.mediaserver;

import java.io.IOException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.AbstractControlUnit;
import de.neo.smarthome.api.Event;
import de.neo.smarthome.api.IControl;
import de.neo.smarthome.api.IControlCenter;
import de.neo.smarthome.api.IControlUnit.EventException;
import de.neo.smarthome.api.IMediaServer;
import de.neo.smarthome.api.IPlayer;
import de.neo.smarthome.api.PlayerException;

public class MediaControlUnit extends AbstractControlUnit {

	private MediaServerImpl mMediaServer;

	public MediaControlUnit(IControlCenter center) {
		super(center);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getRemoteableControlInterface() throws RemoteException {
		return IMediaServer.class;
	}

	@Override
	public IMediaServer getRemoteableControlObject() {
		return mMediaServer;
	}

	@Override
	public void initialize(Element element) throws SAXException, IOException {
		super.initialize(element);
		mMediaServer = new MediaServerImpl();
		mMediaServer.initialize(element);
	}

	@Override
	public boolean performEvent(Event event) throws RemoteException,
			EventException {
		try {
			String action = event.getParameter("action");
			String value = event.getParameter("value");
			String player = event.getParameter("player");
			IPlayer remotePlayer = null;
			if (action == null)
				throw new EventException(
						"Parameter action (play|playList|pause|stop|volume|shutdown) missing to execute media event!");
			if ("mplayer".equals(player))
				remotePlayer = mMediaServer.getMPlayer();
			else if ("omxplayer".equals(player))
				remotePlayer = mMediaServer.getOMXPlayer();
			else if ("totem".equals(player))
				remotePlayer = mMediaServer.getTotemPlayer();

			IControl control = mMediaServer.getControl();

			switch (action) {
			case "play":
				remotePlayer.play(value);
				break;
			case "playList":
				remotePlayer.playPlayList(value);
				break;
			case "pause":
				remotePlayer.playPause();
				break;
			case "vol":
			case "volume":
				if ("up".equals(value))
					remotePlayer.volUp();
				else if ("down".equals(value))
					remotePlayer.volDown();
				else
					remotePlayer.setVolume(Integer.parseInt(value));
				break;
			case "stop":
			case "quit":
				remotePlayer.quit();
				break;
			case "shutdown":
				control.shutdown();
			default:
				throw new EventException("Unknown player action: " + action);
			}

		} catch (RemoteException | PlayerException e) {
			throw new EventException(e.getClass().getSimpleName() + ": "
					+ e.getMessage());
		}
		return true;
	}
}
