package de.remote.desktop.menus;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.mediaserver.api.IControl;

/**
 * Menu to handle standard commands for the computer. Provides items to handle
 * display brightness and to shutdown the remote computer.
 * 
 * @author sebastian
 */
public class ControlMenu extends Menu {

	/**
	 * generated id
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * remote control object
	 */
	private IControl control;

	/**
	 * allocates new menu, create and initialize menu items.
	 */
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

	/**
	 * set the remote control object
	 * 
	 * @param control
	 */
	public void setControl(IControl control) {
		this.control = control;
	}
}