package de.neo.remote.mediaserver;

import java.io.IOException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.neo.remote.AbstractControlUnit;
import de.neo.remote.api.IMediaServer;
import de.neo.rmi.protokol.RemoteException;

public class MediaControlUnit extends AbstractControlUnit {

	private MediaServerImpl mMediaServer;

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

}
