package de.neo.remote.desktop.panels;

import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import de.neo.remote.mediaserver.api.IPlayer;
import de.neo.remote.mediaserver.api.PlayerException;
import de.neo.rmi.protokol.RemoteException;

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
	private JButton play;

	/**
	 * next button
	 */
	private JButton next;

	/**
	 * previous button
	 */
	private JButton prev;

	/**
	 * fullscreen button
	 */
	private JButton fullscreen;

	/**
	 * volume up button
	 */
	private JButton volUp;

	/**
	 * volume down button
	 */
	private JButton volDown;

	/**
	 * quit player button
	 */
	private JButton quit;

	/**
	 * seek forward button
	 */
	private JButton seekFWB;

	/**
	 * seek backward button
	 */
	private JButton seekBWB;

	/**
	 * shuffle button
	 */
	private JButton shuffle;

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
		this.play = new JButton("|>");
		this.next = new JButton(">>|");
		this.prev = new JButton("|<<");
		this.seekFWB = new JButton(">>");
		this.seekBWB = new JButton("<<");
		this.fullscreen = new JButton("Fullscreen");
		this.volUp = new JButton("Vol+");
		this.volDown = new JButton("Vol-");
		this.quit = new JButton("Quit");
		this.shuffle = new JButton("Shuffle");
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
			private boolean isFullscreen ;

			public void actionPerformed(ActionEvent e) {
				try {
					PlayerPanel.this.player.fullScreen(isFullscreen = !isFullscreen);
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