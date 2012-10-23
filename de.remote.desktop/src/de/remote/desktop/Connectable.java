package de.remote.desktop;

/**
 * The connectable interface is able to connect to given registry ip.
 * 
 * @author sebastian
 */
public interface Connectable {

	/**
	 * Connect to given registry ip.
	 * 
	 * @param registry
	 */
	public void connectToServer(String registry);

}
