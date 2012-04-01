package de.remote.desktop.menus;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import de.remote.desktop.ControlFrame;
import de.remote.desktop.panels.ServerPanel;

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
	 * the map contains all server as key with ip as value
	 */
	private Map<String, String> serverMap;

	/**
	 * allocates new menu, create and initialize menu items
	 * 
	 * @param mainFrame
	 */
	public ServerMenu(ControlFrame mainFrame) {
		super("Server");
		this.mainFrame = mainFrame;
		updateServers();
	}

	/**
	 * update list of available servers
	 */
	public void updateServers() {
		removeAll();
		serverMap = ServerPanel.loadServers(ServerPanel.SERVER_FILE);
		for (String name : serverMap.keySet()) {
			MenuItem item = new MenuItem(name);
			item.addActionListener(new ServerActionListener(serverMap.get(name)));
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

		public ServerActionListener(String ip) {
			this.newServerIp = ip;
		}

		public void actionPerformed(ActionEvent e) {
			ServerMenu.this.mainFrame.connectToServer(this.newServerIp);
		}
	}
}