package de.remote.mobile.activies;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import de.remote.mobile.R;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IChatListener;
import de.remote.mobile.services.RemoteService;
import de.remote.mobile.services.RemoteService.PlayerBinder;

/**
 * the chatactivity provides functions to chat with other clients connected to
 * the server. It shows a textarea with the conversation. the activity listens
 * for new messages.
 * 
 * @author sebastian
 */
public class ChatActivity extends Activity implements IChatListener {

	/**
	 * textarea for conversation
	 */
	private EditText chatArea;

	/**
	 * textfield for new message
	 */
	private EditText chatInput;

	/**
	 * binder for connection with service
	 */
	private PlayerBinder binder;

	/**
	 * hander to post actions from any thread
	 */
	private Handler handler = new Handler();

	/**
	 * button to post an own message
	 */
	private Button postButton;

	/**
	 * connection with service
	 */
	private ServiceConnection playerConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			try {
				binder.getChatServer().removeChatListener(ChatActivity.this);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (PlayerBinder) service;
			disableScreen();
			if (binder.getChatServer() == null)
				binder.connectToServer("192.168.1.3", new EnableAreaRunnable());
			else
				new EnableAreaRunnable().run();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		findComponents();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = new Intent(this, RemoteService.class);
		startService(intent);
		boolean bound = bindService(intent, playerConnection,
				Context.BIND_AUTO_CREATE);
		if (!bound)
			Log.e("nicht verbunden!!!", "service nicht verbunden");
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (binder != null)
			try {
				binder.getChatServer().removeChatListener(this);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		unbindService(playerConnection);
	}

	/**
	 * find all components by their id
	 */
	private void findComponents() {
		chatArea = (EditText) findViewById(R.id.txt_chat_area);
		chatInput = (EditText) findViewById(R.id.txt_chat_input);
		postButton = (Button) findViewById(R.id.btn_chat_post);
		chatArea.setEnabled(false);
	}

	/**
	 * post an own message
	 * 
	 * @param v
	 */
	public void post(View v) {
		String msg = chatInput.getText().toString();
		chatInput.setText("");
		try {
			binder.getChatServer().postMessage("Android", msg);
		} catch (RemoteException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * disapble gui elements
	 */
	private void disableScreen() {
		chatArea.setEnabled(false);
		chatInput.setEnabled(false);
		postButton.setEnabled(false);
	}

	/**
	 * enable gui elements
	 */
	public void enableArea() {
		chatInput.setEnabled(true);
		postButton.setEnabled(true);
	}

	@Override
	public void informMessage(final String client, final String msg)
			throws RemoteException {
		handler.post(new Runnable() {

			@Override
			public void run() {
				String area = chatArea.getText().toString();
				area += "   " + client + "\n" + msg + "\n";
				chatArea.setText(area);
			}
		});
	}

	@Override
	public void informNewClient(String client) throws RemoteException {

	}

	@Override
	public void informLeftClient(String client) throws RemoteException {

	}

	@Override
	public String getName() throws RemoteException {
		return "Android";
	}

	/**
	 * runnable to start chat. it enables the gui elements.
	 * 
	 * @author sebastian
	 */
	public class EnableAreaRunnable implements Runnable {
		@Override
		public void run() {
			enableArea();
			try {
				binder.getChatServer().addChatListener(ChatActivity.this);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
}