package de.remote.mobile.activies;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IChatListener;
import de.remote.mobile.R;
import de.remote.mobile.services.RemoteService;
import de.remote.mobile.services.RemoteService.PlayerBinder;
import de.remote.mobile.util.ChatAdapter;

/**
 * the chatactivity provides functions to chat with other clients connected to
 * the server. It shows a textarea with the conversation. the activity listens
 * for new messages.
 * 
 * @author sebastian
 */
public class ChatActivity extends Activity {

	/**
	 * name of chat properties
	 */
	public static final String CHAT_PROPERTIES = "chatProperties";

	/**
	 * name of the attribute for the client name
	 */
	public static final String CLIENT_NAME = "clientName";

	/**
	 * name of the attribute with all messages
	 */
	public static final String MESSAGE_LIST = "messageList";

	/**
	 * area for conversation
	 */
	private ListView chatArea;

	/**
	 * textfield for new message
	 */
	private EditText chatInput;

	/**
	 * binder for connection with service
	 */
	private PlayerBinder binder;

	/**
	 * the chat listener will be informed about events on the chat server
	 */
	private IChatListener listener = new ChatListener();

	/**
	 * hander to post actions from any thread
	 */
	private Handler handler = new Handler();

	/**
	 * button to post an own message
	 */
	private Button postButton;

	/**
	 * format for date
	 */
	private SimpleDateFormat formatter = new SimpleDateFormat("(HH:mm:ss)");
	
	/**
	 * this list contains all messages
	 */
	private ArrayList<Message> messages = new ArrayList<Message>();

	/**
	 * name of the chat client
	 */
	private String clientName;

	/**
	 * connection with service
	 */
	private ServiceConnection playerConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			try {
				binder.getChatServer().removeChatListener(listener);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (PlayerBinder) service;
			disableScreen();
			if (binder.isConnected())
				new EnableAreaRunnable().run();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		SharedPreferences sharedPreferences = getSharedPreferences(
				CHAT_PROPERTIES, 0);
		clientName = sharedPreferences.getString(CLIENT_NAME, "Android");
		findComponents();
		chatArea.setAdapter(new ChatAdapter(this, messages));
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = new Intent(this, RemoteService.class);
		startService(intent);
		bindService(intent, playerConnection, Context.BIND_AUTO_CREATE);

		chatArea.setAdapter(new ChatAdapter(ChatActivity.this, messages));
		chatArea.setSelection(messages.size());
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (binder != null && binder.getChatServer() != null)
			try {
				binder.getChatServer().removeChatListener(listener);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		unbindService(playerConnection);
	}

	/**
	 * find all components by their id
	 */
	private void findComponents() {
		chatArea = (ListView) findViewById(R.id.list_chat);
		chatArea.setScrollingCacheEnabled(false);
		chatArea.setCacheColorHint(0);
		chatInput = (EditText) findViewById(R.id.txt_chat_input);
		postButton = (Button) findViewById(R.id.btn_chat_post);
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
			binder.getChatServer().postMessage(clientName, msg);
		} catch (RemoteException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * disapble gui elements
	 */
	private void disableScreen() {
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
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(MESSAGE_LIST, messages);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		messages = (ArrayList<Message>) savedInstanceState
				.getSerializable(MESSAGE_LIST);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.opt_chat_clear:
			messages.clear();
			chatArea.setAdapter(new ChatAdapter(ChatActivity.this, messages));
			return true;
		case R.id.opt_chat_name:
			Intent intent = new Intent(this, GetTextActivity.class);
			startActivityForResult(intent, GetTextActivity.RESULT_CODE);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GetTextActivity.RESULT_CODE && data != null
				&& data.getExtras() != null) {
			String newName = data.getExtras().getString(GetTextActivity.RESULT);
			SharedPreferences.Editor editor = getSharedPreferences(
					CHAT_PROPERTIES, 0).edit();
			editor.putString(CLIENT_NAME, newName);
			editor.commit();
			try {
				binder.getChatServer().removeChatListener(listener);
				clientName = newName;
				binder.getChatServer().addChatListener(listener);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());
		mi.inflate(R.menu.chat_pref, menu);
		return true;
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
				binder.getChatServer().addChatListener(listener);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * this object represents a message with author and message
	 * 
	 * @author sebastian
	 */
	public class Message implements Serializable {

		/**
		 * generated id
		 */
		private static final long serialVersionUID = -3004935219876939234L;

		/**
		 * author of the message
		 */
		public String author;

		/**
		 * text of the message
		 */
		public String message;

		/**
		 * time of the message
		 */
		public String date;

		public Message(String client, String msg, String date) {
			author = client;
			message = msg;
			this.date = date;
		}
	}

	/**
	 * the chat listener listenes on the chat server and will be informed about
	 * events such as new/old clients or messages.
	 * 
	 * @author sebastian
	 */
	public class ChatListener implements IChatListener {

		@Override
		public void informMessage(final String client, final String msg,
				final Date time) throws RemoteException {
			handler.post(new Runnable() {

				@Override
				public void run() {
					Message message = new Message(client, msg, formatter.format(time));
					messages.add(message);
					chatArea.setAdapter(new ChatAdapter(ChatActivity.this,
							messages));
					chatArea.setSelection(messages.size());
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
			return clientName;
		}
	}
}
