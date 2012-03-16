package de.newsystem.rmi.protokol;

import java.io.Serializable;

/**
 * request protocol contains object id, method name and parameters
 * 
 * @author sebastian
 * 
 */
public class Request implements Serializable {

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
	private boolean oneway;

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

	public boolean isOneway() {
		return oneway;
	}

	public void setOneway(boolean oneway) {
		this.oneway = oneway;
	}
	

}
