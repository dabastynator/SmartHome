package de.neo.rmi.protokol;

import java.io.Serializable;

/**
 * request protocol contains object id, method name and parameters
 * 
 * @author sebastian
 * 
 */
public class Request implements Serializable {

	public enum Type{
		CLOSE, NORMAL, ONEWAY
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2837953654619994379L;

	/**
	 * object id
	 */
	private String objectId;

	/**
	 * method name
	 */
	private String methodName;

	/**
	 * parameter array
	 */
	private Object[] parameters;
	
	/**
	 * this request is oneway or not
	 */
	private Type type;

	/**
	 * 
	 * allcoates new request with id and method name
	 * 
	 * @param id
	 * @param methodName
	 */
	public Request(String id, String methodName) {
		this.objectId = id;
		this.methodName = methodName;
	}

	public String getObject() {
		return objectId;
	}

	public void setObject(String object) {
		this.objectId = object;
	}

	public String getMethod() {
		return methodName;
	}

	public void setMethod(String method) {
		this.methodName = method;
	}

	public Object[] getParams() {
		return parameters;
	}

	public void setParams(Object[] params) {
		this.parameters = params;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	public Type getType(){
		return type;
	}

}
