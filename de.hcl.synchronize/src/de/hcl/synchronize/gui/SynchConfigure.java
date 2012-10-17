package de.hcl.synchronize.gui;

import java.awt.FlowLayout;
import java.awt.Panel;
import java.io.File;
import java.io.IOException;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

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

	/**
	 * Allocate new configure
	 */
	public SynchConfigure() {
		setName("Sessions");
		try {
			File file = new File(CONFIG_FILES);
			if (!file.exists())
				file.createNewFile();
			iniFile = new IniFile(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		TableModel m = new SessionModel();
		setLayout(new FlowLayout());
		JTable table = new JTable(m);
		add(new JScrollPane(table));
	}

	private class SessionModel extends AbstractTableModel {
		@Override
		public String getColumnName(int column) {
			if (column == 0)
				return "Session";
			if (column == 1)
				return "Client";
			if (column == 2)
				return "Location";

			return super.getColumnName(column);
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			int i = 0;
			for (String sec : iniFile.getSections()) {
				if (rowIndex == i && columnIndex == 0)
					return sec;
				i++;
				for (String client : iniFile.getKeySet(sec)) {
					if (rowIndex == i && columnIndex == 1)
						return client;
					if (rowIndex == i && columnIndex == 2)
						return iniFile.getPropertyString(sec, client, "");
					i++;
				}
			}
			return "";
		}

		public int getRowCount() {
			int i = 0;
			for (String sec : iniFile.getSections()) {
				i += 1 + iniFile.getKeySet(sec).size();
			}
			return i;
		}

		public int getColumnCount() {
			return 3;
		}
	}

}
