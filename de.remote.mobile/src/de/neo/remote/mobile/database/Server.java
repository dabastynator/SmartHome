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
	 * index of the id column
	 */
	int INDEX_ID = 0;
	
	/**
	 * name of the server 
	 */
	String NAME = "name";
	
	/**
	 * index of the name column
	 */
	int INDEX_NAME = 1;
	
	/**
	 * ip of the server
	 */
	String IP = "ip";
	
	/**
	 * index of the ip column
	 */
	int INDEX_IP = 2;
	
	/**
	 * mark a server as favorite
	 */
	String FAVORITE = "favorite";
	
	/**
	 * index of the favorite column
	 */
	int INDEX_FAVORITE = 3;

}
