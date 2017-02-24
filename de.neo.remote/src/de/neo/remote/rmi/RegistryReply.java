package de.neo.remote.rmi;

import java.io.Serializable;


/**
 * reply of the registry
 * @author sebastian
 */
public class RegistryReply implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7056967395008703045L;
	
	/**
	 * global object
	 */
	private GlobalObject object;

	public GlobalObject getObject() {
		return object;
	}

	public void setObject(GlobalObject object) {
		this.object = object;
	}
	
}
