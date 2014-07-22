package de.neo.rmi.api;

public interface IRegistryConnection {

	public void onRegistryConnected(Server server);
	
	public void onRegistryLost();

	public boolean isManaged();
}
