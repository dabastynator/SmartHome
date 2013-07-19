package de.remote.desktop.menus;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import de.remote.desktop.ControlFrame;
import de.remote.mediaserver.api.IPlayer;

/**
 * Menu to switch the current player. There are two possible players: mplayer
 * and totem.
 * 
 * @author sebastian
 */
public class PlayerMenu extends Menu {
	
	/**
	 * generated id
	 */
	private static final long serialVersionUID = -3270281115030502334L;
	
	/**
	 * reference to the main frame to inform about new player 
	 */
	private ControlFrame mainFraim;
	
	/**
	 * mplayer object 
	 */
	private IPlayer mplayer;
	
	/**
	 * totem object 
	 */
	private IPlayer totem;

	/**
	 * allocates menu, create and initialize menu items
	 * @param mainFrame
	 */
	public PlayerMenu(ControlFrame mainFrame) {
		super("Player");
		this.mainFraim = mainFrame;
		MenuItem mplayerItem = new MenuItem("MPlayer");
		mplayerItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PlayerMenu.this.mainFraim.setPlayer(PlayerMenu.this.mplayer);
			}
		});
		MenuItem totemItem = new MenuItem("Totem");
		totemItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PlayerMenu.this.mainFraim.setPlayer(PlayerMenu.this.totem);
			}
		});
		add(mplayerItem);
		add(totemItem);
	}

	/**
	 * set player objects
	 * @param totemPlayer
	 * @param mPlayer
	 */
	public void setPlayer(IPlayer totemPlayer, IPlayer mPlayer) {
		this.totem = totemPlayer;
		this.mplayer = mPlayer;
	}
}