package de.remote.mobile.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import de.remote.mobile.R;
import de.remote.mobile.database.ServerDatabase;

/**
 * the activity provides functionality to create a new server. it reads the
 * input fields and inserts a new row in the server table.
 * 
 * @author sebastian
 */
public class NewServerActivity extends Activity {

	/**
	 * text field for server name
	 */
	private EditText name;

	/**
	 * text field for server ip
	 */
	private EditText ip;

	/**
	 * database object
	 */
	private ServerDatabase serverDB;

	/**
	 * if editing an old server, the name will be stored in this field.
	 */
	private int oldServer = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newserver);
		findComponents();
		oldServer = -1;
		serverDB = new ServerDatabase(this);
		if (getIntent().getExtras() != null
				&& getIntent().getExtras().containsKey(
						BrowserActivity.EXTRA_SERVER_ID)) {
			int server = getIntent().getExtras().getInt(
					BrowserActivity.EXTRA_SERVER_ID);
			String ip = serverDB.getIpOfServer(server);
			String name = serverDB.getNameOfServer(server);
			oldServer = server;
			this.name.setText(name);
			this.ip.setText(ip);
		}
	}

	/**
	 * search text fields by id
	 */
	private void findComponents() {
		name = (EditText) findViewById(R.id.txt_server_name);
		ip = (EditText) findViewById(R.id.txt_server_ip);
	}

	/**
	 * insert new server in server table
	 * 
	 * @param view
	 */
	public void createServer(View view) {
		String serverName = name.getText().toString();
		if (oldServer >= 0)
			serverDB.deleteServer(oldServer);
		int id = (int) serverDB.insertServer(serverName, ip.getText().toString());
		Toast.makeText(this, "server '" + serverName + "' added",
				Toast.LENGTH_SHORT).show();
		Intent i = new Intent(this, BrowserActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		i.putExtra(BrowserActivity.EXTRA_SERVER_ID, id);
		startActivity(i);
		finish();
	}

	@Override
	protected void onDestroy() {
		serverDB.close();
		super.onDestroy();
	}
}
