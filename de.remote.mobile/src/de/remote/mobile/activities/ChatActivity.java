package de.remote.mobile.activities;

import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
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
import de.remote.mobile.util.ChatAdapter;

/**
 * the chatactivity provides functions to chat with other clients connected to
 * the server. It shows a textarea with the conversation. the activity listens
 * for new messages.
 * 
 * @author sebastian
 */
public class ChatActivity extends BindedActivity {

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
	 * name for extra data for server name
	 */
	public static final String EXTRA_SERVER_ID = "serverId";

	/**
	 * area for conversation
	 */
	private ListView chatArea;

	/**
	 * textfield for new message
	 */
	private EditText chatInput;

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
	 * this list contains all messages
	 */
	private ArrayList<Message> messages = new ArrayList<Message>();

	/**
	 * name of the chat client
	 */
	private String clientName;

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

	@Override
	protected void startConnecting() {
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
		outState.putParcelableArrayList(MESSAGE_LIST, messages);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		messages = savedInstanceState.getParcelableArrayList(MESSAGE_LIST);
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
			intent.putExtra(GetTextActivity.DEFAULT_TEXT, clientName);
			startActivityForResult(intent, GetTextActivity.RESULT_CODE);
			return true;
		case R.id.opt_webcam:
			intent = new Intent(this, WebcamActivity.class);
			startActivity(intent);
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
			} catch (Exception e) {
				Toast.makeText(ChatActivity.this, e.getMessage(),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * this object represents a message with author and message
	 * 
	 * @author sebastian
	 */
	public class Message implements Parcelable {

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

		/**
		 * allocate new message bean
		 * 
		 * @param client
		 * @param msg
		 * @param date
		 */
		public Message(String client, String msg, String date) {
			author = client;
			message = msg;
			this.date = date;
		}

		@Override
		public int describeContents() {
			return hashCode();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(author);
			dest.writeString(message);
			dest.writeString(date);
		}
	}

	/**
	 * message creator reads parcel source and creates a message message
	 * 
	 * @author sebastian
	 */
	public class MessageCreator implements Parcelable.Creator<Message> {

		@Override
		public Message createFromParcel(Parcel source) {
			return new Message(source.readString(), source.readString(),
					source.readString());
		}

		@Override
		public Message[] newArray(int size) {
			return new Message[size];
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
				final String time) throws RemoteException {
			handler.post(new Runnable() {

				@Override
				public void run() {
					Message message = new Message(client, msg, '(' + time + ')');
					messages.add(message);
					chatArea.setAdapter(new ChatAdapter(ChatActivity.this,
							messages));
					chatArea.setSelection(messages.size());
				}
			});
		}

		@Override
		public void informNewClient(final String client) throws RemoteException {
			handler.post(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(ChatActivity.this,
							"The client '" + client + "' has join the chat",
							Toast.LENGTH_LONG).show();
				}
			});
		}

		@Override
		public void informLeftClient(final String client) throws RemoteException {
			handler.post(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(ChatActivity.this,
							"The client '" + client + "' has left the chat",
							Toast.LENGTH_LONG).show();
				}
			});			
		}

		@Override
		public String getName() throws RemoteException {
			return clientName;
		}
	}

	@Override
	void binderConnected() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void onServerConnectionChanged(String serverName) {
		try {
			if (binder != null && binder.isConnected()) {
				if (binder.getChatServer() != null) {
					binder.getChatServer().addChatListener(new ChatListener());
					enableArea();
				} else {
					Toast.makeText(this, "no chat server available",
							Toast.LENGTH_SHORT).show();
					startConnecting();
				}
			} else
				startConnecting();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	

}
