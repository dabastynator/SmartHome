package de.remote.mobile.database;

/**
 * interface for power, containing the table name and fields.
 * 
 * @author sebastian
 */
public interface PowerSwitch {

	/**
	 * name of the table
	 */
	public static final String TABLE_NAME = "power";
	
	/**
	 * primary identifier in the table
	 */
	String ID = "_id";
	
	/**
	 * index of the id column
	 */
	int INDEX_ID = 0;
	
	/**
	 * name of the switch
	 */
	String NAME = "name";
	
	/**
	 * index of the name column
	 */
	int INDEX_NAME = 1;
	
	/**
	 * the switch
	 */
	String SWITCH = "switch";
	
	/**
	 * index of the switch column
	 */
	int INDEX_SWITCH = 2;
}
