package de.remote.mobile.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteStatement;

/**
 * helper class to manage the database with the server table.
 * 
 * @author sebastian
 */
public class PowerSwitchDao {

	private RemoteDatabase database;

	public PowerSwitchDao(RemoteDatabase serverDatabase) {
		this.database = serverDatabase;
	}

	public String getNameOfSwitch(int switcH) {
		SQLiteDatabase db = database.getReadableDatabase();
		SQLiteStatement stmt = db
				.compileStatement(PowerSwitchTable.SQL_NAME_FROM_SWITCH);
		stmt.bindLong(1, switcH);
		String name = null;
		try {
			name = stmt.simpleQueryForString();
		} catch (SQLiteDoneException e) {
			name = null;
		}
		db.close();
		return name;
	}

	public void setSwitchName(int switcH, String name) {
		SQLiteDatabase db = database.getReadableDatabase();
		SQLiteStatement stmt = db
				.compileStatement(PowerSwitchTable.SQL_DELETE_SWITCH);
		stmt.bindLong(1, switcH);
		stmt.execute();
		ContentValues cv = new ContentValues();
		cv.put(PowerSwitchTable.NAME, name);
		cv.put(PowerSwitchTable.SWITCH, switcH);
		db.insert(PowerSwitchTable.TABLE_NAME, null, cv);
		db.close();
	}

}
