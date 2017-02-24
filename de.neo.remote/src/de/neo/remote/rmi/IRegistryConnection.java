package de.neo.remote.rmi;

import de.neo.remote.rmi.Server;

public interface IRegistryConnection {

	public void onRegistryConnected(Server server);
	
	public void onRegistryLost();

	public boolean isManaged();
}
