package de.neo.remote.desktop;

import java.awt.BorderLayout;
import java.awt.MenuBar;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import de.neo.remote.controlcenter.api.IControlCenter;
import de.neo.remote.desktop.menus.ControlMenu;
import de.neo.remote.desktop.menus.PlayerMenu;
import de.neo.remote.desktop.menus.RegistryMenu;
import de.neo.remote.desktop.panels.BrowserPanel;
import de.neo.remote.desktop.panels.ChatPanel;
import de.neo.remote.desktop.panels.PlayListPanel;
import de.neo.remote.desktop.panels.PlayerPanel;
import de.neo.remote.desktop.panels.RegistryPanel;
import de.neo.remote.desktop.panels.WebcamPanel;
import de.neo.remote.mediaserver.api.IBrowser;
import de.neo.remote.mediaserver.api.IChatServer;
import de.neo.remote.mediaserver.api.IMediaServer;
import de.neo.remote.mediaserver.api.IPlayList;
import de.neo.remote.mediaserver.api.IPlayer;
import de.neo.remote.mediaserver.api.IPlayerListener;
import de.neo.remote.mediaserver.api.PlayerException;
import de.neo.remote.mediaserver.api.PlayingBean;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.protokol.RemoteException;

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
public class ControlFrame extends JFrame implements Connectable {

	/**
	 * generated id
	 */
	private static final long serialVersionUID = 1222897455250462547L;

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
	 * chat panel with textfields do write and get messages
	 */
	private ChatPanel chat;

	/**
	 * the webcam panel displays a remote webcam
	 */
	private WebcamPanel webcam;

	/**
	 * menu to choose different server to connect with
	 */
	private RegistryMenu serverMenu;

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
	public IMediaServer station;
	
	/**
	 * remote music station list object to get all other remote music station
	 */
	public IControlCenter stationList;


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

	/**
	 * the server editor provides functionality to edit the server list.
	 */
	private RegistryPanel serverEditor;

	/**
	 * allocate frame and create all panels and menus
	 */
	public ControlFrame() {
		super("Remote Control");
		setDefaultCloseOperation(3);
		setSize(760, 400);

		loadIcon();

		this.controlMenu = new ControlMenu();
		this.serverMenu = new RegistryMenu(this);
		this.playerMenu = new PlayerMenu(this);

		this.playerControl = new PlayerPanel();
		this.chat = new ChatPanel();
		this.plsBrowser = new PlayListPanel();
		this.fileBrowser = new BrowserPanel();
		this.serverEditor = new RegistryPanel(serverMenu);
		this.webcam = new WebcamPanel();
		this.playerListener = new DesktopPlayerListener();

		setLayout(new BorderLayout());
		add(BorderLayout.SOUTH, this.playerControl);

		JTabbedPane tabs = new JTabbedPane();
		add(BorderLayout.CENTER, tabs);
		tabs.add(this.fileBrowser);
		tabs.add(this.plsBrowser);
		tabs.add(this.chat);
		tabs.add(this.serverEditor);
		tabs.add(this.webcam);
		setVisible(true);
		addWindowListener(new DesktopWindowListener());

		MenuBar menuBar = new MenuBar();
		menuBar.add(this.controlMenu);
		menuBar.add(this.serverMenu);
		menuBar.add(this.playerMenu);
		setMenuBar(menuBar);
	}

	/**
	 * load icon for program
	 */
	private void loadIcon() {
		// first argument is location on local testing
		// second argument is in jar file
		String[] res = { "res/icon.png", "./icon.png" };

		InputStream base = null;
		for (String str : res) {
			base = getClass().getClassLoader().getResourceAsStream(str);
			if (base != null)
				break;
		}

		try {
			BufferedImage image = ImageIO.read(base);
			setIconImage(image);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			stationList = ((IControlCenter) server.find(IControlCenter.ID,
					IControlCenter.class));
			station = (IMediaServer) stationList.getControlUnit(0).getRemoteableControlObject();
			mPlayer = station.getMPlayer();
			totemPlayer = station.getTotemPlayer();
			IBrowser browser = station.createBrowser();
			mPlayer.addPlayerMessageListener(playerListener);
			try {
				playerListener.playerMessage(mPlayer.getPlayingBean());
			} catch (PlayerException e) {
				e.printStackTrace();
			}
			fileBrowser.setBrowser(browser);
			IPlayList playList = station.getPlayList();
			fileBrowser.setPlayList(playList);
			plsBrowser.setPlayList(playList);
			chatServer = (IChatServer) server.find(IChatServer.ID,
					IChatServer.class);
			chat.setClientName(name);
			chat.setChatServer(chatServer);
			setPlayer(mPlayer);
			playerMenu.setPlayer(totemPlayer, mPlayer);
			controlMenu.setControl(station.getControl());
			webcam.registerWebcamListener();
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

	@Override
	public void connectToServer(String registry) {
		connectToServer(registry, this.port, this.clientName);
	}

	public class DesktopPlayerListener implements IPlayerListener {
		public DesktopPlayerListener() {
		}

		public void playerMessage(PlayingBean playing) throws RemoteException {
			if (playing == null)
				return;
			System.out.println(playing.getTitle());
			ControlFrame.this.setTitle(playing.getArtist() + " - "
					+ playing.getTitle());
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
			ControlFrame.this.server.close();
		}
	}
}