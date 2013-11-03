package de.neo.remote.desktop.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import de.neo.remote.desktop.menus.RegistryMenu;

/**
 * the server panel enables to edit all servers.
 * 
 * @author sebastian
 */
public class RegistryPanel extends Panel {

	/**
	 * location of the server file, that contains all server in CSV format.
	 */
	public static final String SERVER_FILE = ".remoteserver";

	/**
	 * generated id
	 */
	private static final long serialVersionUID = -4784113498246481222L;

	/**
	 * awt list that shows all servers
	 */
	private List serverList;

	/**
	 * name of selected server
	 */
	private TextField name;

	/**
	 * ip of selected server
	 */
	private TextField ip;

	/**
	 * the map contains all server as key with ip as value
	 */
	private Map<String, String> serverMap = new HashMap<String, String>();

	/**
	 * current selection in the server list
	 */
	private TextField selectedServer;

	/**
	 * the menu must be updated.
	 */
	private RegistryMenu serverMenu;

	/**
	 * allocate and initialize tab to edit all available server.
	 * 
	 * @param serverMenu
	 */
	public RegistryPanel(RegistryMenu serverMenu) {
		setName("Registries");
		this.serverMenu = serverMenu;
		setLayout(new BorderLayout());
		add(BorderLayout.CENTER, createServerArea());
		add(BorderLayout.NORTH, createButtons());
		serverMap = loadServers(SERVER_FILE);
		showServers();
	}

	/**
	 * create and initialize area to edit displayed server.
	 * 
	 * @return component
	 */
	private Component createButtons() {
		JButton showButton = new JButton("Show details");
		JButton newButton = new JButton("Create new server");
		JButton editButton = new JButton("Apply changes");
		JButton deleteButton = new JButton("Delete server");
		showButton.addActionListener(new ShowDetailListener());
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Component source = (Component) e.getSource();
				String server = JOptionPane.showInputDialog(source,
						"Enter new server name");
				String newIp = JOptionPane.showInputDialog(source,
						"Enter ip of the server");
				serverMap.put(server, newIp);
				serverList.add(server);
				name.setText(server);
				ip.setText(newIp);
				saveServers(SERVER_FILE);
				showServers();
			}
		});
		editButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				serverMap.remove(selectedServer);
				serverMap.put(name.getText(), ip.getText());
				saveServers(SERVER_FILE);
				showServers();
			}
		});
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				serverMap.remove(serverList.getSelectedItem());
				saveServers(SERVER_FILE);
				showServers();
			}
		});
		Panel area = new Panel();
		area.setLayout(new FlowLayout());
		area.add(showButton);
		area.add(newButton);
		area.add(editButton);
		area.add(deleteButton);
		return area;
	}

	/**
	 * load the server file and return the map
	 * 
	 * @param serverFile
	 */
	public static Map<String, String> loadServers(String serverFile) {
		Map<String, String> servers = new HashMap<String, String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(
					serverFile)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String name = line.substring(0, line.indexOf(";"));
				String ip = line.substring(line.indexOf(";") + 1);
				servers.put(name, ip);
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		return servers;
	}

	/**
	 * show all servers in the server list
	 */
	private void showServers() {
		serverList.removeAll();
		for (String name : serverMap.keySet())
			serverList.add(name);
		serverMenu.updateServers();
	}

	/**
	 * write content of serverMap to given file in CSV format.
	 * 
	 * @param serverFile
	 */
	private void saveServers(String serverFile) {
		File server = new File(serverFile);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(server));
			for (String name : serverMap.keySet())
				writer.append(name + ";" + serverMap.get(name) + "\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * create and initialize area to show all server.
	 * 
	 * @return component
	 */
	private Component createServerArea() {
		serverList = new List();
		serverList.addActionListener(new ShowDetailListener());
		Panel detail = createDetailArea();
		Panel area = new Panel();
		area.setLayout(new GridLayout());
		area.add(serverList);
		area.add(detail);
		return area;
	}

	/**
	 * Create and initialize area to show and edit details of selected registry
	 * entry.
	 * 
	 * @return component
	 */
	private Panel createDetailArea() {
		Panel holder = new Panel();
		holder.setLayout(new FlowLayout());
		Panel detail = new Panel();
		name = new TextField(15);
		ip = new TextField(15);
		detail.setLayout(new GridLayout(2, 2, 10, 10));
		detail.add(new Label("Name: "));
		detail.add(name);
		detail.add(new Label("IP: "));
		detail.add(ip);
		holder.add(detail);
		return holder;
	}

	/**
	 * the listener shows details of the current selected server.
	 * 
	 * @author sebastian
	 */
	private class ShowDetailListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String server = serverList.getSelectedItem();
			selectedServer = name;
			name.setText(server);
			ip.setText(serverMap.get(server));
		}
	}
}
