package de.remote.mobile.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import de.remote.mobile.R;
import de.remote.mobile.database.ServerDatabase;
import de.remote.mobile.database.ServerTable;
import de.remote.mobile.util.ServerCursorAdapter;

/**
 * this activity shows all configured servers from the server database. by
 * clicking to an item, the browser activity starts a connection to this server.
 * 
 * @author sebastian
 */
public class SelectServerActivity extends ListActivity {

	/**
	 * request code for this activity
	 */
	public static final int RESULT_CODE = 2;

	/**
	 * name of the server name attribute
	 */
	public static final String SERVER_ID = "serverID";

	/**
	 * store the current selected server
	 */
	protected int selectedItem;

	/**
	 * database object to execute changes
	 */
	private ServerDatabase serverDB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		serverDB = new ServerDatabase(this);
		SQLiteDatabase db = serverDB.getReadableDatabase();
		Cursor c = db.query(ServerTable.TABLE_NAME, ServerTable.ALL_COLUMNS,
				null, null, null, null, null);
		startManagingCursor(c);

		setListAdapter(new ServerCursorAdapter(this, c));
		db.close();
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) {
				Intent i = new Intent();
				SQLiteCursor c = (SQLiteCursor) getListAdapter().getItem(
						position);
				selectedItem = c.getInt(ServerTable.INDEX_ID);
				i.putExtra(SERVER_ID, selectedItem);
				setResult(RESULT_CODE, i);
				finish();
			}

		});
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				SQLiteCursor c = (SQLiteCursor) getListAdapter().getItem(
						position);
				selectedItem = c.getInt(ServerTable.INDEX_ID);
				return false;
			}
		});
		registerForContextMenu(getListView());
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater mi = new MenuInflater(getApplication());
		mi.inflate(R.menu.server_pref, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.opt_server_delete:
			serverDB.deleteServer(selectedItem);
			updateList();
			break;
		case R.id.opt_server_edit:
			Intent intent = new Intent(SelectServerActivity.this,
					NewServerActivity.class);
			intent.putExtra(BrowserActivity.EXTRA_SERVER_ID, selectedItem);
			startActivity(intent);
			finish();
			break;
		case R.id.opt_server_favorite:
			serverDB.setFavorite(selectedItem);
			updateList();
			break;
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * update the list view
	 */
	private void updateList() {
		ServerCursorAdapter adapter = (ServerCursorAdapter) getListAdapter();
		SQLiteDatabase db = serverDB.getReadableDatabase();
		Cursor c = db.query(ServerTable.TABLE_NAME, ServerTable.ALL_COLUMNS,
				null, null, null, null, null);
		adapter.changeCursor(c);
		db.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());
		mi.inflate(R.menu.select_server_pref, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.opt_select_server_add:
			Intent intent = new Intent(this, NewServerActivity.class);
			startActivity(intent);
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onDestroy() {
		serverDB.close();
		super.onDestroy();
	}

}
