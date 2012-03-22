package de.remote.mobile.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

/**
 * helper class to manage the database with the server table.
 * 
 * @author sebastian
 */
public class ServerDatabase extends SQLiteOpenHelper {

	/**
	 * name of the database
	 */
	private static final String DATABASE_NAME = "remote_server.db";

	/**
	 * current version
	 */
	private static final int DATABASE_VERSION = 1;

	/**
	 * allocate the sql helper
	 * 
	 * @param context
	 */
	public ServerDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(ServerTable.SQL_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(ServerTable.SQL_DROP);
		onCreate(db);
	}

	/**
	 * delete all servers with equal name.
	 * 
	 * @param name
	 */
	public void deleteServer(String name) {
		SQLiteDatabase db = getReadableDatabase();
		SQLiteStatement stmt = db
				.compileStatement(ServerTable.SQL_DELETE_SERVER);
		stmt.bindString(1, name);
		stmt.execute();
		db.close();
	}

	/**
	 * delete all servers with equal name.
	 * 
	 * @param name
	 * @return ip
	 */
	public String getIpOfServer(String name) {
		SQLiteDatabase db = getReadableDatabase();
		SQLiteStatement stmt = db
				.compileStatement(ServerTable.SQL_IP_FROM_SERVER);
		stmt.bindString(1, name);
		String ip = stmt.simpleQueryForString();
		db.close();
		return ip;
	}

	/**
	 * get favorite server
	 * 
	 * @return server
	 */
	public String getFavoriteServer() {
		SQLiteDatabase db = getReadableDatabase();
		SQLiteStatement stmt = db
				.compileStatement(ServerTable.SQL_FAVORITE_SERVER);
		String server = null;
		try {
			server = stmt.simpleQueryForString();
		} catch (SQLiteDoneException e) {
			server = null;
		}
		db.close();
		return server;
	}

	/**
	 * insert new server
	 * 
	 * @param name
	 * @param ip
	 */
	public void insertServer(String serverName, String ip) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(ServerTable.NAME, serverName);
		cv.put(ServerTable.IP, ip);
		db.insert(ServerTable.TABLE_NAME, null, cv);
		db.close();
	}

	/**
	 * set server to favorite server
	 * 
	 * @param serverName
	 */
	public void setFavorite(String serverName) {
		SQLiteDatabase db = getWritableDatabase();
		try {
			SQLiteStatement stmt = db
					.compileStatement(ServerTable.SQL_SET_FAVORITE);
			stmt.bindString(1, serverName);
			stmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.close();
	}

}
