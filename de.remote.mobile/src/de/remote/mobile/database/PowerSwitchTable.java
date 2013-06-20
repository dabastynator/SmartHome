package de.remote.mobile.database;


/**
 * helper class with sql statments for power switch table.
 * 
 * @author sebastian
 */
public class PowerSwitchTable implements PowerSwitch {

	/**
	 * sql statement to create the table
	 */
	public static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "("
			+ ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME
			+ " TEXT NOT NULL, " + SWITCH + " INT);";

	/**
	 * sql statement to delete the table.
	 */
	public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

	/**
	 * array with all column names of the table
	 */
	public static final String[] ALL_COLUMNS = new String[] { ID, NAME, SWITCH };

	public static final String SQL_NAME_FROM_SWITCH = "SELECT " + NAME + " FROM "
			+ TABLE_NAME + " WHERE " + SWITCH + " = ?";
	
	public static final String SQL_DELETE_SWITCH = "DELETE FROM " + TABLE_NAME
			+ " WHERE " + SWITCH + " = ?";
	
}
