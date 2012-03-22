package de.remote.mobile.database;

/**
 * interface for server, containing the table name and fields.
 * 
 * @author sebastian
 */
public interface Server {

	/**
	 * name of the table
	 */
	public static final String TABLE_NAME = "server";

	/**
	 * primary identifier in table
	 */
	String ID = "_id";
	
	/**
	 * name of the server 
	 */
	String NAME = "name";
	
	/**
	 * ip of the server
	 */
	String IP = "ip";
	
	/**
	 * mark a server as favorite
	 */
	String FAVORITE = "favorite";

}
