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

	/**
	 * generated id
	 */
	private static final long serialVersionUID = -1645107804597739638L;

	/**
	 * remote player object
	 */
	private IPlayer player;

	/**
	 * play button
	 */
	private Button play;

	/**
	 * next button
	 */
	private Button next;

	/**
	 * previous button
	 */
	private Button prev;

	/**
	 * fullscreen button
	 */
	private Button fullscreen;

	/**
	 * volume up button
	 */
	private Button volUp;

	/**
	 * volume down button
	 */
	private Button volDown;

	/**
	 * quit player button
	 */
	private Button quit;

	/**
	 * seek forward button
	 */
	private Button seekFWB;

	/**
	 * seek backward button
	 */
	private Button seekBWB;
	
	/**
	 * shuffle button
	 */
	private Button shuffle;
	
	/**
	 * true if shuffle is active, otherwise false
	 */
	private boolean isShuffle = false;
	
	/**
	 * allocate playerpanel, create and initialize all buttons
	 */
	public PlayerPanel() {
		setName("Player control");
		setLayout(new GridLayout());
		this.play = new Button("|>");
		this.next = new Button(">>|");
		this.prev = new Button("|<<");
		this.seekFWB = new Button(">>");
		this.seekBWB = new Button("<<");
		this.fullscreen = new Button("Fullscreen");
		this.volUp = new Button("Vol+");
		this.volDown = new Button("Vol-");
		this.quit = new Button("Quit");
		this.shuffle = new Button("Shuffle");
		add(this.prev);
		add(this.seekBWB);
		add(this.play);
		add(this.seekFWB);
		add(this.next);
		add(this.fullscreen);
		add(this.volUp);
		add(this.volDown);
		add(this.shuffle);
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
		this.seekBWB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					PlayerPanel.this.player.seekBackwards();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				} catch (PlayerException e1) {
					e1.printStackTrace();
				}
			}
		});
		this.seekFWB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					PlayerPanel.this.player.seekForwards();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				} catch (PlayerException e1) {
					e1.printStackTrace();
				}
			}
		});
		this.fullscreen.addActionListener(new ActionListener() {
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
		this.shuffle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					isShuffle = !isShuffle;
					player.useShuffle(isShuffle);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				} catch (PlayerException e1) {
					e1.printStackTrace();
				}
			}
		});
	}

	/**
	 * set remote player object
	 * 
	 * @param player
	 */
	public void setPlayer(IPlayer player) {
		this.player = player;
	}
}