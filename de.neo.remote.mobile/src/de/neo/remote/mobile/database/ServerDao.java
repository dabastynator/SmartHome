package de.neo.remote.mobile.database;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteStatement;

public class ServerDao {

	
	private RemoteDatabase database;

	public ServerDao(RemoteDatabase serverDatabase) {
		this.database = serverDatabase;
	}

	/**
	 * delete the server with the given id
	 * 
	 * @param id of the server to delete
	 */
	public void deleteServer(int id) {
		SQLiteDatabase db = database.getReadableDatabase();
		SQLiteStatement stmt = db
				.compileStatement(ServerTable.SQL_DELETE_SERVER);
		stmt.bindLong(1, id);
		stmt.execute();
		db.close();
	}

	/**
	 * get ip of server with given id
	 * 
	 * @param id of the server
	 * @return ip
	 */
	public String getIpOfServer(int id) {
		SQLiteDatabase db = database.getReadableDatabase();
		SQLiteStatement stmt = db
				.compileStatement(ServerTable.SQL_IP_FROM_SERVER);
		stmt.bindLong(1, id);
		String ip = stmt.simpleQueryForString();
		db.close();
		return ip;
	}
	
	/**
	 * get the name of the server with given id
	 * @param id
	 * @return name
	 */
	public String getNameOfServer(int id) {
		SQLiteDatabase db = database.getReadableDatabase();
		SQLiteStatement stmt = db
				.compileStatement(ServerTable.SQL_NAME_FROM_SERVER);
		stmt.bindLong(1, id);
		String name = stmt.simpleQueryForString();
		db.close();
		return name;
	}

	/**
	 * get favorite server
	 * 
	 * @return server
	 */
	public int getFavoriteServer() {
		SQLiteDatabase db = database.getReadableDatabase();
		SQLiteStatement stmt = db
				.compileStatement(ServerTable.SQL_FAVORITE_SERVER);
		int server = -1;
		try {
			server = Integer.parseInt(stmt.simpleQueryForString());
		} catch (SQLiteDoneException e) {
			server = -1;
		}
		db.close();
		return server;
	}

	/**
	 * insert new server
	 * 
	 * @param mName
	 * @param ip
	 */
	public long insertServer(String serverName, String ip) {
		SQLiteDatabase db = database.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(ServerTable.NAME, serverName);
		cv.put(ServerTable.IP, ip);
		long id = db.insert(ServerTable.TABLE_NAME, null, cv);
		db.close();
		return id;
	}

	/**
	 * set server to favorite server
	 * 
	 * @param id of the server
	 */
	public void setFavorite(int id) {
		SQLiteDatabase db = database.getWritableDatabase();
		try {
			SQLiteStatement stmt = db
					.compileStatement(ServerTable.SQL_SET_FAVORITE);
			stmt.bindLong(1, id);
			stmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.close();
	}
	
}
