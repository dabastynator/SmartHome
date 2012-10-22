package de.hcl.synchronize.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import de.hcl.synchronize.client.MainSynchClient;
import de.hcl.synchronize.util.IniFile;

/**
 * The configure panel edits the sessions and their clients.
 * 
 * @author sebastian
 */
public class SynchConfigure extends Panel {

	/**
	 * Generated id
	 */
	private static final long serialVersionUID = -2611923680638524002L;

	/**
	 * The configure file contains all session ids, clients and location to
	 * synchronize.
	 */
	public static final String CONFIG_FILES = ".synch_config.ini";

	/**
	 * The inifile contains all session ids and client
	 */
	private IniFile iniFile;

	private JTable table;

	private SessionModel sessionTableModel;

	/**
	 * Allocate new configure
	 */
	public SynchConfigure() {
		setName("Clients");
		try {
			File file = new File(CONFIG_FILES);
			if (!file.exists())
				file.createNewFile();
			iniFile = new IniFile(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		sessionTableModel = new SessionModel();
		setLayout(new BorderLayout());
		table = new JTable(sessionTableModel);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int row = table.getSelectedRow();
					int col = table.getSelectedColumn();
					if (row == -1 || col == -1)
						return;
					String client = (String) iniFile.getSections().toArray()[row];
					System.out.println(client);
					String oldValue = "null";
					switch (col) {
					case 0:
						oldValue = "Session ID";
						break;
					case 1:
						oldValue = "Client Name";
						break;
					case 2:
						oldValue = "Location";
						break;
					case 3:
						oldValue = "Read only";
						break;
					case 4:
						oldValue = "Listen";
						break;
					}
					String value = JOptionPane.showInputDialog(null,
							"Enter new value: ", oldValue, 1);
					if (value == null)
						return;
					switch (col) {
					case 0:
						iniFile.setPropertyString(client,
								MainSynchClient.SESSION_ID, value);
						break;
					case 1:
						iniFile.setPropertyString(client,
								MainSynchClient.CLIENT_NAME, value);
						break;
					case 2:
						iniFile.setPropertyString(client,
								MainSynchClient.LOCATION, value);
						break;
					case 3:
						iniFile.setPropertyBool(client,
								MainSynchClient.READ_ONLY,
								value.equalsIgnoreCase("true"));
						break;
					case 4:
						iniFile.setPropertyBool(client,
								MainSynchClient.REGISTER_LISTENER,
								value.equalsIgnoreCase("true"));
						break;
					}
				}
				super.mouseClicked(e);
			}
		});
		add(BorderLayout.CENTER, new JScrollPane(table));
		add(BorderLayout.SOUTH, createButtons());
	}

	/**
	 * Create and configure button area.
	 * 
	 * @return button area
	 */
	private Component createButtons() {
		Panel p = new Panel();
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					iniFile.writeFile();
				} catch (IOException e1) {
				}
			}
		});
		p.add(saveButton);
		JButton addButton = new JButton("New");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String clientID = "client";
				int i = 0;
				while (iniFile.getSections().contains(clientID + i))
					i++;
				iniFile.setPropertyString(clientID + i,
						MainSynchClient.CLIENT_NAME, "New Client");
				sessionTableModel.fireTableDataChanged();
			}
		});
		p.add(addButton);
		JButton removeButton = new JButton("Delete");
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				if (row == -1)
					return;
				iniFile.removeSection((String) iniFile.getSections().toArray()[row]);
				sessionTableModel.fireTableDataChanged();
			}
		});
		p.add(removeButton);
		return p;
	}

	private class SessionModel extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public String getColumnName(int column) {
			if (column == 0)
				return "Session";
			if (column == 1)
				return "Client";
			if (column == 2)
				return "Location";
			if (column == 3)
				return "Read only";
			if (column == 4)
				return "File system listen";

			return super.getColumnName(column);
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			String clientID = (String) iniFile.getSections().toArray()[rowIndex];
			switch (columnIndex) {
			case 0:
				return iniFile.getPropertyString(clientID,
						MainSynchClient.SESSION_ID, "null");
			case 1:
				return iniFile.getPropertyString(clientID,
						MainSynchClient.CLIENT_NAME, "null");
			case 2:
				return iniFile.getPropertyString(clientID,
						MainSynchClient.LOCATION, "null");
			case 3:
				return iniFile.getPropertyBool(clientID,
						MainSynchClient.READ_ONLY, true);
			case 4:
				return iniFile.getPropertyBool(clientID,
						MainSynchClient.REGISTER_LISTENER, false);
			default:
				return "";
			}
		}

		public int getRowCount() {
			return iniFile.getSections().size();
		}

		public int getColumnCount() {
			return 5;
		}
	}

}
