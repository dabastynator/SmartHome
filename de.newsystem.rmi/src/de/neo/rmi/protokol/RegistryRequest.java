package de.neo.rmi.protokol;

import java.io.Serializable;

/**
 * request to the registry
 * 
 * @author sebastian
 */
public class RegistryRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6235938941187178961L;

	/**
	 * types of the action
	 * 
	 * @author sebastian
	 */
	public enum Type {
		REGISTER, FIND, UNREGISTER
	}

	/**
	 * action type
	 */
	private Type type;

	/**
	 * global object
	 */
	private GlobalObject object;

	/**
	 * id of an object
	 */
	private String id;

	public RegistryRequest(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public GlobalObject getObject() {
		return object;
	}

	public void setObject(GlobalObject object) {
		this.object = object;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
