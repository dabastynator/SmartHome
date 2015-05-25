package de.neo.remote;

import java.io.IOException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.neo.remote.api.IControlUnit;
import de.neo.rmi.protokol.RemoteException;

public abstract class AbstractControlUnit implements IControlUnit {

	protected String mName;
	protected String mDescription;
	protected float[] mPosition;
	protected String mID;

	@Override
	public String getName() throws RemoteException {
		return mName;
	}

	@Override
	public String getDescription() throws RemoteException {
		return mDescription;
	}

	@Override
	public float[] getPosition() throws RemoteException {
		return mPosition;
	}

	@Override
	public String getID() throws RemoteException {
		return mID;
	}

	public void initialize(Element element) throws SAXException, IOException {
		for (String attribute : new String[] { "id", "name", "type", "x", "y",
				"z" })
			if (!element.hasAttribute(attribute))
				throw new SAXException(attribute + " missing for "
						+ getClass().getSimpleName());
		mID = element.getAttribute("id");
		mName = element.getAttribute("name");
		mDescription = element.getAttribute("type");
		mPosition = new float[] { Float.parseFloat(element.getAttribute("x")),
				Float.parseFloat(element.getAttribute("y")),
				Float.parseFloat(element.getAttribute("z")) };
	}
}
