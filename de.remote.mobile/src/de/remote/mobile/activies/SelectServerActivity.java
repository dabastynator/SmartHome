package de.remote.mobile.activies;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.LinearLayout;
import android.widget.TextView;
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
	 * store the current selected server
	 */
	protected String selectedItem;

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
			public void onItemClick(AdapterView<?> arg0, View view, int arg2,
					long arg3) {
				String item = getServerOfRow(view);
				Intent intent = new Intent(SelectServerActivity.this,
						BrowserActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
				intent.putExtra(BrowserActivity.EXTRA_SERVER_NAME, item);
				startActivity(intent);
				finish();
			}

		});
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View view,
					int arg2, long arg3) {
				selectedItem = getServerOfRow(view);
				return false;
			}
		});
		registerForContextMenu(getListView());
	}

	/**
	 * get the server name of the selected view element.
	 * 
	 * @param view
	 * @return servername
	 */
	private String getServerOfRow(View view) {
		LinearLayout row = (LinearLayout) view;
		LinearLayout col = (LinearLayout) row.getChildAt(1);
		TextView name = (TextView) col.getChildAt(0);
		return name.getText().toString();
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
			intent.putExtra(BrowserActivity.EXTRA_SERVER_NAME, selectedItem);
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
			finish();
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
