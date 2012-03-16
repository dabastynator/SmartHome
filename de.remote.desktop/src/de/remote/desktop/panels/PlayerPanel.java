package de.remote.desktop.panels;

import java.awt.Button;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IPlayer;
import de.remote.api.PlayerException;

/**
 * the playerpanel contains buttons to control the current player.
 * 
 * @author sebastian
 */
public class PlayerPanel extends Panel {
	private IPlayer player;
	private Button play;
	private Button next;
	private Button prev;
	private Button full;
	private Button volUp;
	private Button volDown;
	private Button quit;

	public PlayerPanel() {
		setName("Player control");
		setLayout(new GridLayout());
		this.play = new Button("Play");
		this.next = new Button("Next");
		this.prev = new Button("Previous");
		this.full = new Button("Fullscreen");
		this.volUp = new Button("Vol+");
		this.volDown = new Button("Vol-");
		this.quit = new Button("Quit");
		add(this.play);
		add(this.next);
		add(this.prev);
		add(this.full);
		add(this.volUp);
		add(this.volDown);
		add(this.quit);
		this.play.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					PlayerPanel.this.player.playPause();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				} catch (PlayerException e1) {
					e1.printStackTrace();
				}
			}
		});
		this.next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					PlayerPanel.this.player.next();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				} catch (PlayerException e1) {
					e1.printStackTrace();
				}
			}
		});
		this.prev.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					PlayerPanel.this.player.previous();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				} catch (PlayerException e1) {
					e1.printStackTrace();
				}
			}
		});
		this.full.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					PlayerPanel.this.player.fullScreen();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				} catch (PlayerException e1) {
					e1.printStackTrace();
				}
			}
		});
		this.volUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					PlayerPanel.this.player.volUp();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				} catch (PlayerException e1) {
					e1.printStackTrace();
				}
			}
		});
		this.volDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					PlayerPanel.this.player.volDown();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				} catch (PlayerException e1) {
					e1.printStackTrace();
				}
			}
		});
		this.quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					PlayerPanel.this.player.quit();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				} catch (PlayerException e1) {
					e1.printStackTrace();
				}
			}
		});
	}

	public void setPlayer(IPlayer player) {
		this.player = player;
	}
}