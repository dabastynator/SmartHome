package de.neo.remote.action;

import java.io.IOException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.neo.remote.AbstractControlUnit;
import de.neo.remote.api.Event;
import de.neo.remote.api.ICommandAction;
import de.neo.remote.api.IControlCenter;
import de.neo.rmi.protokol.RemoteException;

public class ActionControlUnit extends AbstractControlUnit {

	public ActionControlUnit(IControlCenter center) {
		super(center);
	}

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
		mCommandAction = new CommandAction(this);
		mCommandAction.initialize(element);
	}

	@Override
	public boolean performEvent(Event event) throws RemoteException,
			EventException {
		try {
			String action = event.getParameter("action");
			if (action == null)
				throw new EventException(
						"Parameter action (start|stop) missing to execute command event!");
			if (action.equalsIgnoreCase("start"))
				mCommandAction.startAction();
			else if (action.equalsIgnoreCase("stop"))
				mCommandAction.stopAction();
			else
				throw new EventException(
						"Unknown parameter value for action event! Excpected: start|stop");
		} catch (IOException e) {
			throw new EventException(e.getClass().getSimpleName() + ": "
					+ e.getMessage());
		}
		return true;
	}
}
