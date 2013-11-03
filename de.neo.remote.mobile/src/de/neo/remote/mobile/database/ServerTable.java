package de.neo.remote.mobile.database;

/**
 * helper class with sql statements for server table.
 * 
 * @author sebastian
 */
public class ServerTable implements Server {

	/**
	 * sql to create the table
	 */
	public static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "("
			+ ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME
			+ " TEXT NOT NULL, " + IP + " TEXT NOT NULL, " + FAVORITE
			+ " INT);";

	/**
	 * sql to delete the table
	 */
	public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

	/**
	 * array with all columns of the table
	 */
	public static final String[] ALL_COLUMNS = new String[] { ID, NAME, IP,
			FAVORITE };

	/**
	 * delete server by id
	 */
	public static final String SQL_DELETE_SERVER = "DELETE FROM " + TABLE_NAME
			+ " WHERE " + ID + " = ?";

	/**
	 * get ip from server by id
	 */
	public static final String SQL_IP_FROM_SERVER = "SELECT " + IP + " FROM "
			+ TABLE_NAME + " WHERE " + ID + " = ?";
	
	/**
	 * get name from server by id
	 */
	public static final String SQL_NAME_FROM_SERVER = "SELECT " + NAME + " FROM "
			+ TABLE_NAME + " WHERE " + ID + " = ?";

	public static final String SQL_FAVORITE_SERVER = "SELECT " + ID
			+ " FROM " + TABLE_NAME + " WHERE " + FAVORITE + " = 1";

	/**
	 * set favorite 1 if name equals, otherwise set favorite 0
	 */
	public static final String SQL_SET_FAVORITE = "UPDATE " + TABLE_NAME
			+ " SET " + FAVORITE + " = (" + ID + " = ?)";
}
