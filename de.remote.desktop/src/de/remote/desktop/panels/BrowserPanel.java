package de.remote.desktop.panels;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.List;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IBrowser;
import de.remote.api.IPlayList;
import de.remote.api.IPlayer;
import de.remote.api.PlayerException;

/**
 * panel to browse throw the file system. the panel has a buttons to control the
 * player and edit playlists.
 * 
 * @author sebastian
 */
public class BrowserPanel extends Panel {
	private IBrowser browser;
	private IPlayer player;
	private String[] directories;
	private String[] files;
	private List fileList;
	private IPlayList pls;

	public BrowserPanel() {
		setName("File-Browser");
		this.fileList = new List();
		this.fileList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (BrowserPanel.this.fileList.getSelectedIndex() == 0) {
						BrowserPanel.this.browser.goBack();
						BrowserPanel.this.updateContent();
						return;
					}
					for (String d : BrowserPanel.this.directories)
						if (d.equals(BrowserPanel.this.fileList
								.getSelectedItem())) {
							BrowserPanel.this.browser
									.goTo(BrowserPanel.this.fileList
											.getSelectedItem());
							BrowserPanel.this.updateContent();
							return;
						}
					String file = BrowserPanel.this.browser.getFullLocation()
							+ BrowserPanel.this.fileList.getSelectedItem();
					BrowserPanel.this.player.play(file);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			}
		});
		setLayout(new BorderLayout());
		add("Center", this.fileList);

		add("North", createButtons());
	}

	private Component createButtons() {
		Button startFrom = new Button("Play selected element");
		startFrom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String file = BrowserPanel.this.browser.getFullLocation()
							+ BrowserPanel.this.fileList.getSelectedItem();
					BrowserPanel.this.player.play(file);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			}
		});
		Button addPls = new Button("Add to Playlist");
		addPls.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Component source = (Component) e.getSource();
				try {
					String[] plsList = BrowserPanel.this.pls.getPlayLists();
					Object response = JOptionPane.showInputDialog(source,
							"Select Playlist",
							"Select the Playlist that will be extended", 3,
							null, plsList, plsList[0]);
					if (response != null)
						BrowserPanel.this.pls.extendPlayList(
								(String) response,
								BrowserPanel.this.browser.getFullLocation()
										+ BrowserPanel.this.fileList
												.getSelectedItem());
				} catch (HeadlessException e1) {
					e1.printStackTrace();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				} catch (PlayerException e1) {
					e1.printStackTrace();
				}
			}
		});
		Panel buttons = new Panel();
		buttons.setLayout(new GridLayout());
		buttons.add(startFrom);
		buttons.add(addPls);
		return buttons;
	}

	public void setBrowser(IBrowser browser) {
		this.browser = browser;
		updateContent();
	}

	public void setPlayer(IPlayer player) {
		this.player = player;
	}

	private void updateContent() {
		this.fileList.removeAll();
		this.fileList.add("←");
		try {
			this.directories = this.browser.getDirectories();
			this.files = this.browser.getFiles();
			for (String d : this.directories)
				this.fileList.add(d);
			for (String f : this.files)
				this.fileList.add(f);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void setPlayList(IPlayList playList) {
		this.pls = playList;
	}
}