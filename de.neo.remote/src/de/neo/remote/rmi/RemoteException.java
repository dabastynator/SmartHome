package de.neo.remote.rmi;

/**
 * exception for remote method invocation
 * 
 * @author sebastian
 */
public class RemoteException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6803659830395623055L;

	public RemoteException(String message) {
		super(message);
	}

	public RemoteException(String message, Exception cause) {
		super(message, cause);
	}

}
