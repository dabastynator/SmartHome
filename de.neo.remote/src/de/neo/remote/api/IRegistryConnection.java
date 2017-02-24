package de.neo.remote.api;

public interface IRegistryConnection {

	public void onRegistryConnected(Server server);
	
	public void onRegistryLost();

	public boolean isManaged();
}
