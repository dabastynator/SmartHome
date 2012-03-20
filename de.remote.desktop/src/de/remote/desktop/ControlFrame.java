package de.remote.desktop;

import java.awt.BorderLayout;
import java.awt.MenuBar;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IBrowser;
import de.remote.api.IChatServer;
import de.remote.api.IPlayList;
import de.remote.api.IPlayer;
import de.remote.api.IPlayerListener;
import de.remote.api.IStation;
import de.remote.api.PlayingBean;
import de.remote.desktop.menus.ControlMenu;
import de.remote.desktop.menus.PlayerMenu;
import de.remote.desktop.menus.ServerMenu;
import de.remote.desktop.panels.BrowserPanel;
import de.remote.desktop.panels.ChatPanel;
import de.remote.desktop.panels.PlayListPanel;
import de.remote.desktop.panels.PlayerPanel;

/**
 * The main frame for the gui. It creates all panels and menus that will be
 * needed.
 * 
 * @author sebastian
 */
/**
 * @author sebastian
 *
 */
/**
 * @author sebastian
 * 
 */
public class ControlFrame extends JFrame {

	/**
	 * generated id
	 */
	private static final long serialVersionUID = 1222897455250462547L;

	/**
	 * list of all connectable servers
	 */
	public static final Map<String, String> serverList = new HashMap<String, String>();

	/**
	 * control panel for the player
	 */
	private PlayerPanel playerControl;

	/**
	 * browser panel to display directories
	 */
	private BrowserPanel fileBrowser;

	/**
	 * playlist panel to display playlists and items of playlists
	 */
	private PlayListPanel plsBrowser;

	/**
	 * chat panel with textfiels do write and get messages
	 */
	private ChatPanel chat;

	/**
	 * menu to choose different server to connect with
	 */
	private ServerMenu serverMenu;

	/**
	 * menu to choose different player
	 */
	private PlayerMenu playerMenu;

	/**
	 * menu for main control options, example shutdown
	 */
	private ControlMenu controlMenu;

	/**
	 * listener for the player
	 */
	private DesktopPlayerListener playerListener;

	/**
	 * player object for the remote player
	 */
	private IPlayer currentPlayer;

	/**
	 * remote factory object to get all other remote objects
	 */
	public IStation station;

	/**
	 * remote mplayer
	 */
	private IPlayer mPlayer;

	/**
	 * remote totem
	 */
	private IPlayer totemPlayer;

	/**
	 * chat server object
	 */
	private IChatServer chatServer;

	/**
	 * the local server
	 */
	public Server server;

	/**
	 * port for the local server
	 */
	private int port;

	/**
	 * client name used for chat client
	 */
	private String clientName;

	static {
		serverList.put("Idefix", "192.168.1.4");
		serverList.put("Inspiron", "192.168.1.3");
		serverList.put("localhost", "localhost");
	}

	/**
	 * allocate frame and create all panels and menus
	 */
	public ControlFrame() {
		setDefaultCloseOperation(3);
		setSize(600, 400);
		setTitle("Idefix");
		this.playerControl = new PlayerPanel();
		this.chat = new ChatPanel();
		this.plsBrowser = new PlayListPanel();
		this.fileBrowser = new BrowserPanel();
		this.playerListener = new DesktopPlayerListener();

		setLayout(new BorderLayout());
		add("South", this.playerControl);

		JTabbedPane tabs = new JTabbedPane();
		add(tabs);
		tabs.add(this.fileBrowser);
		tabs.add(this.plsBrowser);
		tabs.add(this.chat);
		setVisible(true);
		addWindowListener(new DesktopWindowListener());

		MenuBar menuBar = new MenuBar();
		this.controlMenu = new ControlMenu();
		this.serverMenu = new ServerMenu(this);
		this.playerMenu = new PlayerMenu(this);
		menuBar.add(this.controlMenu);
		menuBar.add(this.serverMenu);
		menuBar.add(this.playerMenu);
		setMenuBar(menuBar);
	}

	/**
	 * set new current player. playerpanel, browserpanel and plspanel will be
	 * informed.
	 * 
	 * @param player
	 */
	public void setPlayer(IPlayer player) {
		this.currentPlayer = player;
		this.fileBrowser.setPlayer(player);
		this.plsBrowser.setPlayer(player);
		this.playerControl.setPlayer(player);
	}

	/**
	 * create new connection to server, read objects from new registry. start
	 * new local server at given port.
	 * 
	 * @param registry
	 * @param port
	 * @param name
	 */
	public void connectToServer(String registry, int port, String name) {
		try {
			closeConnections();
			this.port = port;
			clientName = name;
			setTitle("connecting...");
			server = Server.getServer();
			server.connectToRegistry(registry);
			server.startServer(port);
			station = ((IStation) server.find("de.newsystem.idefix.station",
					IStation.class));
			mPlayer = station.getMPlayer();
			totemPlayer = station.getTotemPlayer();
			IBrowser browser = station.createBrowser();
			mPlayer.addPlayerMessageListener(playerListener);
			fileBrowser.setBrowser(browser);
			IPlayList playList = station.getPlayList();
			fileBrowser.setPlayList(playList);
			plsBrowser.setPlayList(playList);
			chatServer = station.getChatServer();
			chat.setClientName(name);
			chat.setChatServer(chatServer);
			setPlayer(mPlayer);
			playerMenu.setPlayer(totemPlayer, mPlayer);
			controlMenu.setControl(station.getControl());
			for (String serverName : serverList.keySet())
				if (((String) serverList.get(serverName)).equals(registry))
					setTitle(serverName);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * close connection from server, remove listeners.
	 */
	private void closeConnections() {
		try {
			if (this.mPlayer != null)
				this.mPlayer.removePlayerMessageListener(this.playerListener);
		} catch (Exception localException) {
		}
		try {
			this.chat.removeListener();
		} catch (Exception localException1) {
		}
		try {
			Server.getServer().close();
		} catch (Exception localException2) {
		}
	}

	public void connectToServer(String registry) {
		connectToServer(registry, this.port, this.clientName);
	}

	public class DesktopPlayerListener implements IPlayerListener {
		public DesktopPlayerListener() {
		}

		public void playerMessage(PlayingBean playing) throws RemoteException {
			ControlFrame.this.setTitle("Idefix - " + playing.getArtist()
					+ " - " + playing.getTitle());
		}
	}

	/**
	 * listener to handle exit of the window, unregister listener from player
	 * and chat server
	 * 
	 * @author sebastian
	 */
	public class DesktopWindowListener extends WindowAdapter {
		public DesktopWindowListener() {
		}

		public void windowClosing(WindowEvent e) {
			super.windowClosing(e);
			try {
				if (ControlFrame.this.currentPlayer != null)
					ControlFrame.this.currentPlayer
							.removePlayerMessageListener(ControlFrame.this.playerListener);
				ControlFrame.this.chat.removeListener();
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
			try {
				ControlFrame.this.server.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}