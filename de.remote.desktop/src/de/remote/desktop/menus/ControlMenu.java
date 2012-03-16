package de.remote.desktop.menus;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IControl;

/**
 * menu to handle standard commands for the computer
 * 
 * @author sebastian
 */
public class ControlMenu extends Menu {
	private IControl control;

	public ControlMenu() {
		super("Control");
		MenuItem shutdown = new MenuItem("Shutdown Server");
		add(shutdown);
		shutdown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					ControlMenu.this.control.shutdown();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			}
		});
		MenuItem dark = new MenuItem("Dark Display");
		add(dark);
		dark.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					ControlMenu.this.control.displayDark();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			}
		});
		MenuItem bride = new MenuItem("Bride Display");
		add(bride);
		bride.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					ControlMenu.this.control.displayBride();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			}
		});
	}

	public void setControl(IControl control) {
		this.control = control;
	}
}