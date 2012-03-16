package de.remote.desktop.panels;

import java.awt.BorderLayout;
import java.awt.Button;
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

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IChatListener;
import de.remote.api.IChatServer;

/**
 * a panel to chat with other clients. the panel provides textfiels to show
 * messages and clients and a button to send a message.
 * 
 * @author sebastian
 */
public class ChatPanel extends Panel {
	private List clientList;
	private ChatListener actionListener;
	private TextArea textArea;
	private TextField inputText;
	private IChatServer server;
	private String clientName;

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

	private Component getControl() {
		Panel p = new Panel();
		p.setLayout(new GridLayout());
		this.inputText = new TextField();
		Button b = new Button("Post");
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

	public void removeListener() throws RemoteException {
		if (this.server != null)
			this.server.removeChatListener(this.actionListener);
		this.clientList.removeAll();
	}

	public void setClientName(String name) {
		setName(name + " @ Chat");
		this.clientName = name;
	}

	public class ChatListener implements IChatListener {
		public ChatListener() {
		}

		public void informMessage(String client, String msg)
				throws RemoteException {
			String txt = ChatPanel.this.textArea.getText();
			txt = txt + client + ": " + msg + "\n";
			ChatPanel.this.textArea.setText(txt);
		}

		public void informNewClient(String client) throws RemoteException {
			ChatPanel.this.clientList.add(client);
		}

		public void informLeftClient(String client) throws RemoteException {
			ChatPanel.this.clientList.remove(client);
		}

		public String getName() throws RemoteException {
			return ChatPanel.this.clientName;
		}
	}
}