package de.neo.rmi.protokol;

/**
 * exception for remote method invocation
 * @author sebastian
 */
public class RemoteException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6803659830395623055L;
	
	/**
	 * id of object 
	 */
	private String id;

	public RemoteException(String id,String message) {
		super(message);
		this.id = id;
	}
	
	public String getId(){
		return id;
	}
	
}
