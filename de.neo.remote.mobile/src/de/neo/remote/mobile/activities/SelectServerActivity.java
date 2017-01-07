package de.neo.remote.mobile.activities;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import de.neo.android.persistence.Dao;
import de.neo.android.persistence.DaoException;
import de.neo.android.persistence.DaoFactory;
import de.neo.remote.mobile.persistence.RemoteDaoBuilder;
import de.neo.remote.mobile.persistence.RemoteServer;
import de.neo.remote.mobile.tasks.AbstractTask;
import de.neo.remote.mobile.util.ServerAdapter;
import de.remote.mobile.R;

/**
 * this activity shows all configured servers from the server database. by
 * clicking to an item, the browser activity starts a connection to this server.
 * 
 * @author sebastian
 */
public class SelectServerActivity extends ActionBarActivity {

	public static final int RESULT_CODE = 2;
	public static final String SERVER_ID = "serverID";
	protected RemoteServer mCurrentServer;
	private ListView mServerList;
	private View mNoServerFound;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setLogo(R.drawable.ic_launcher);

		setContentView(R.layout.server);
		DaoFactory.initiate(new RemoteDaoBuilder(this));
		Dao<RemoteServer> dao = DaoFactory.getInstance().getDao(RemoteServer.class);
		findComponents();

		mServerList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
				mCurrentServer = (RemoteServer) adapter.getItemAtPosition(position);
				try {
					setAsFavorite(mCurrentServer);
					Intent i = new Intent();
					i.putExtra(SERVER_ID, (int) mCurrentServer.getId());
					setResult(RESULT_CODE, i);
					finish();
				} catch (DaoException e) {
					new AbstractTask.ErrorDialog(getApplicationContext(), e).show();
				}
			}

		});
		mServerList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long arg3) {
				mCurrentServer = (RemoteServer) adapter.getItemAtPosition(position);
				return false;
			}
		});
		registerForContextMenu(mServerList);
		try {
			if (dao.loadAll().size() == 0)
				editServer(null);
		} catch (DaoException e) {
			e.printStackTrace();
		}
	}

	protected void setAsFavorite(RemoteServer currentServer) throws DaoException {
		Dao<RemoteServer> dao = DaoFactory.getInstance().getDao(RemoteServer.class);
		for (RemoteServer server : dao.loadAll()) {
			server.setFavorite(mCurrentServer.getId() == server.getId());
			dao.update(server);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		updateList();
	}

	private void findComponents() {
		mServerList = (ListView) findViewById(R.id.server_list);
		mNoServerFound = findViewById(R.id.server_no_server_found);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater mi = new MenuInflater(getApplication());
		mi.inflate(R.menu.server_pref, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		try {
			Dao<RemoteServer> dao = DaoFactory.getInstance().getDao(RemoteServer.class);
			switch (item.getItemId()) {
			case R.id.opt_server_delete:
				dao.delete(mCurrentServer.getId());
				updateList();
				break;
			case R.id.opt_server_edit:
				editServer(mCurrentServer);
				break;
			case R.id.opt_server_favorite:
				setAsFavorite(mCurrentServer);
				updateList();
				break;
			}
		} catch (DaoException e) {
			new AbstractTask.ErrorDialog(getApplicationContext(), e).show();
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * update the list view
	 */
	private void updateList() {
		try {
			Dao<RemoteServer> dao = DaoFactory.getInstance().getDao(RemoteServer.class);
			List<RemoteServer> serverList = dao.loadAll();
			ServerAdapter adapter = new ServerAdapter(this, serverList);
			mServerList.setAdapter(adapter);
			if (serverList.size() == 0)
				mNoServerFound.setVisibility(View.VISIBLE);
			else
				mNoServerFound.setVisibility(View.GONE);
		} catch (DaoException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());
		mi.inflate(R.menu.select_server_pref, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.opt_select_server_add:
			addNewServer(null);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void addNewServer(View view) {
		editServer(null);
	}

	private void editServer(final RemoteServer server) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = LayoutInflater.from(this);
		View dialogView = inflater.inflate(R.layout.server_edit, null);
		builder.setView(dialogView);
		final TextView name = (TextView) dialogView.findViewById(R.id.server_edit_name);
		final TextView endpoint = (TextView) dialogView.findViewById(R.id.server_endpoint);
		final TextView apitoken = (TextView) dialogView.findViewById(R.id.server_apitoken);
		if (server != null) {
			builder.setTitle(getString(R.string.server_edit));
			name.setText(server.getName());
			endpoint.setText(server.getEndPoint());
			apitoken.setText(server.getApiToken());
		} else
			builder.setTitle(getString(R.string.server_add_new_server));
		builder.setPositiveButton(getString(R.string.server_post), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				RemoteServer newServer = server;
				if (newServer == null)
					newServer = new RemoteServer();
				newServer.setName(name.getText().toString());
				newServer.setApiToken(apitoken.getText().toString());
				newServer.setEndPoint(endpoint.getText().toString());
				saveServer(newServer, server == null);
			}
		});
		builder.create().show();
	}

	protected void saveServer(RemoteServer newServer, boolean createNew) {
		try {
			Dao<RemoteServer> dao = DaoFactory.getInstance().getDao(RemoteServer.class);
			if (createNew)
				dao.save(newServer);
			else
				dao.update(newServer);
			updateList();
		} catch (DaoException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		DaoFactory.finilize();
		super.onDestroy();
	}

}
