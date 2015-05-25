package de.neo.remote.action;

import java.io.IOException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.neo.remote.AbstractControlUnit;
import de.neo.remote.api.ICommandAction;
import de.neo.rmi.protokol.RemoteException;

public class ActionControlUnit extends AbstractControlUnit {

	private CommandAction mCommandAction;

	@Override
	public Class<?> getRemoteableControlInterface() throws RemoteException {
		return ICommandAction.class;
	}

	@Override
	public ICommandAction getRemoteableControlObject() throws RemoteException {
		return mCommandAction;
	}

	@Override
	public void initialize(Element element) throws SAXException, IOException {
		super.initialize(element);
		mCommandAction = new CommandAction();
		mCommandAction.initialize(element);
	}
}
