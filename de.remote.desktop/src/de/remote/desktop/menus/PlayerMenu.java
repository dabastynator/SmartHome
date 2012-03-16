package de.remote.desktop.menus;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import de.remote.api.IPlayer;
import de.remote.desktop.ControlFrame;

/**
 * menu to switch the current player
 * 
 * @author sebastian
 */
public class PlayerMenu extends Menu {
	private ControlFrame mainFraim;
	private IPlayer mplayer;
	private IPlayer totem;

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

	public void setPlayer(IPlayer totemPlayer, IPlayer mPlayer) {
		this.totem = totemPlayer;
		this.mplayer = mPlayer;
	}
}