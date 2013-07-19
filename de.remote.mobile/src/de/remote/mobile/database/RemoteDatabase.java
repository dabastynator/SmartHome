package de.remote.mobile.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * helper class to manage the database with the server table.
 * 
 * @author sebastian
 */
public class RemoteDatabase extends SQLiteOpenHelper {

	/**
	 * name of the database
	 */
	private static final String DATABASE_NAME = "remote_server.db";

	/**
	 * current version
	 */
	private static final int DATABASE_VERSION = 2;

	
	private ServerDao server;
	
	/**
	 * allocate the sql helper
	 * 
	 * @param context
	 */
	public RemoteDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
//		powerswitch = new PowerSwitchDao(this);
		server = new ServerDao(this);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(ServerTable.SQL_CREATE);
//		db.execSQL(PowerSwitchTable.SQL_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(ServerTable.SQL_DROP);
//		db.execSQL(PowerSwitchTable.SQL_DROP);
		onCreate(db);
	}
	
//	public PowerSwitchDao getPowerSwitchDao(){
//		return powerswitch;
//	}
	
	public ServerDao getServerDao(){
		return server;
	}
}
