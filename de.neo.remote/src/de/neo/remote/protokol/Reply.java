package de.neo.remote.protokol;

import java.io.Serializable;

/**
 * reply of remote method invocation
 * 
 * @author sebastian
 */
public class Reply implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7627778738571303808L;

	/**
	 * result object
	 */
	private Object result;

	/**
	 * result error
	 */
	private Throwable error;

	/**
	 * id of result object
	 */
	private String newId;

	/**
	 * type of result
	 */
	private Class<?> returnType;

	/**
	 * server port
	 */
	private ServerPort serverPort;

	/**
	 * force server to create new adapter for this parameter reply
	 */
	private boolean createNewAdapter;

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Throwable getError() {
		return error;
	}

	public void setError(Throwable error) {
		this.error = error;
	}

	public void addNewId(String id) {
		if (newId == null)
			this.newId = id;
		else
			this.newId += ";" + id;
	}

	public String getNewId() {
		return newId;
	}

	public void setReturnType(Class<?> returnType) {
		this.returnType = returnType;
	}

	public Class<?> getReturnType() {
		return returnType;
	}

	public ServerPort getServerPort() {
		return serverPort;
	}

	public void setServerPort(ServerPort serverPort) {
		this.serverPort = serverPort;
	}

	public boolean isCreateNewAdapter() {
		return createNewAdapter;
	}

	public void setCreateNewAdapter(boolean createNewAdapter) {
		this.createNewAdapter = createNewAdapter;
	}

	
	
}
