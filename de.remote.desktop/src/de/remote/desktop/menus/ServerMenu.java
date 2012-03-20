package de.remote.desktop.menus;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import de.remote.desktop.ControlFrame;

/**
 * Menu to switch the current server. All server are listed in a map of the main
 * frame.
 * 
 * @author sebastian
 */
public class ServerMenu extends Menu {

	/**
	 * generated id
	 */
	private static final long serialVersionUID = -7292966307439063009L;

	/**
	 * main frame to inform about new server to connect with
	 */
	private ControlFrame mainFrame;

	/**
	 * allocates new menu, create and initialize menu items
	 * 
	 * @param mainFrame
	 */
	public ServerMenu(ControlFrame mainFrame) {
		super("Server");
		this.mainFrame = mainFrame;
		for (String name : ControlFrame.serverList.keySet()) {
			MenuItem item = new MenuItem(name);
			item.addActionListener(new ServerActionListener(name));
			add(item);
		}
	}

	/**
	 * listener to inform the main frame about the selected server.
	 * 
	 * @author sebastian
	 */
	public class ServerActionListener implements ActionListener {
		private String newServerIp;

		public ServerActionListener(String server) {
			this.newServerIp = ((String) ControlFrame.serverList.get(server));
		}

		public void actionPerformed(ActionEvent e) {
			ServerMenu.this.mainFrame.connectToServer(this.newServerIp);
		}
	}
}