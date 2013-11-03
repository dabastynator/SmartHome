package de.neo.remote.desktop.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;

import de.neo.remote.mediaserver.api.IChatListener;
import de.neo.remote.mediaserver.api.IChatServer;
import de.newsystem.rmi.protokol.RemoteException;

/**
 * a panel to chat with other clients. the panel provides textfiels to show
 * messages and clients and a button to send a message.
 * 
 * @author sebastian
 */
public class ChatPanel extends Panel {

	/**
	 * generated id
	 */
	private static final long serialVersionUID = 1943882690369058543L;

	/**
	 * list of all clients in the chatroom
	 */
	private List clientList;

	/**
	 * listener for new messages and clients
	 */
	private ChatListener actionListener;

	/**
	 * area for all messages
	 */
	private TextArea textArea;

	/**
	 * textfield for new message
	 */
	private TextField inputText;

	/**
	 * remote chatserver object
	 */
	private IChatServer server;

	/**
	 * name of the client
	 */
	private String clientName;

	/**
	 * allocate chatpanel, create and initialize gui elements
	 */
	public ChatPanel() {
		setName("Chat");
		this.clientList = new List();
		this.actionListener = new ChatListener();
		this.textArea = new TextArea();
		this.textArea.setEditable(false);
		setLayout(new BorderLayout());
		add("West", this.clientList);
		add("Center", this.textArea);
		add("South", getControl());
	}

	/**
	 * create, initialize and return area for new messages, this contains a
	 * textfield and a button.
	 * 
	 * @return control
	 */
	private Component getControl() {
		Panel p = new Panel();
		p.setLayout(new GridLayout());
		this.inputText = new TextField();
		JButton b = new JButton("Post");
		p.add(this.inputText);
		p.add(b);
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (ChatPanel.this.inputText.getText().length() > 0)
						ChatPanel.this.server.postMessage(
								ChatPanel.this.clientName,
								ChatPanel.this.inputText.getText());
					ChatPanel.this.inputText.setText("");
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
				ChatPanel.this.inputText.setText("");
			}
		});
		this.inputText.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				if ((e.getKeyCode() == 10)
						&& (ChatPanel.this.inputText.getText().length() > 0)) {
					try {
						ChatPanel.this.server.postMessage(
								ChatPanel.this.clientName,
								ChatPanel.this.inputText.getText());
					} catch (RemoteException e1) {
						e1.printStackTrace();
					}
					ChatPanel.this.inputText.setText("");
				}
			}
		});
		return p;
	}

	/**
	 * set remote chatserver object
	 * 
	 * @param server
	 */
	public void setChatServer(IChatServer server) {
		this.server = server;
		try {
			String[] allClients = server.getAllClients();
			for (String client : allClients)
				this.clientList.add(client);
			server.addChatListener(this.actionListener);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * remove the listener from the chatserver
	 * 
	 * @throws RemoteException
	 */
	public void removeListener() throws RemoteException {
		if (this.server != null)
			this.server.removeChatListener(this.actionListener);
		this.clientList.removeAll();
	}

	/**
	 * set the name of the client
	 * 
	 * @param name
	 */
	public void setClientName(String name) {
		setName(name + " @ Chat");
		this.clientName = name;
	}

	/**
	 * the chatlistener listens for new messages from the chatserver. in writes
	 * new messages in the textarea.
	 * 
	 * @author sebastian
	 */
	public class ChatListener implements IChatListener {
		
		public ChatListener() {
		}

		@Override
		public void informMessage(String client, String msg, String time)
				throws RemoteException {
			String txt = ChatPanel.this.textArea.getText();
			txt = txt + client + " (" + time + ")\n>" + msg + "\n";
			ChatPanel.this.textArea.setText(txt);
		}

		@Override
		public void informNewClient(String client) throws RemoteException {
			ChatPanel.this.clientList.add(client);
		}

		@Override
		public void informLeftClient(String client) throws RemoteException {
			ChatPanel.this.clientList.remove(client);
		}

		@Override
		public String getName() throws RemoteException {
			return ChatPanel.this.clientName;
		}
	}
}