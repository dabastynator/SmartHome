package de.hcl.synchronize.gui;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import de.hcl.synchronize.client.HCLClient;
import de.remote.desktop.Connectable;
import de.remote.desktop.menus.RegistryMenu;
import de.remote.desktop.panels.RegistryPanel;

/**
 * The synchronizer gui configures sessions, paths,
 * 
 * @author sebastian
 */
public class SynchronizerGui extends JFrame implements Connectable {

	/**
	 * Generated id
	 */
	private static final long serialVersionUID = 5483586975666635359L;

	/**
	 * The registry menu contains all available registry ips and names.
	 */
	private RegistryMenu registryMenu;

	/**
	 * The registry panel configures available registers
	 */
	private RegistryPanel registryConfig;

	/**
	 * The configure panel edits the configure ini file.
	 */
	private SynchConfigure configure;

	public SynchronizerGui() {
		super("Home Cloud");
		setDefaultCloseOperation(3);
		setSize(600, 400);

		loadIcon("icons/cloud.png");

		this.registryMenu = new RegistryMenu(this);

		setLayout(new BorderLayout());
		JTabbedPane tabs = new JTabbedPane();
		add(BorderLayout.CENTER, tabs);
		registryConfig = new RegistryPanel(registryMenu);
		configure = new SynchConfigure();
		tabs.add(configure);
		tabs.add(registryConfig);
	}

	private void loadIcon(String icon) {
		InputStream input;
		try {
			input = HCLClient.inputStreamFromResource(this, icon);
			BufferedImage image = ImageIO.read(input);
			setIconImage(image);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void connectToServer(String registry) {
		// TODO Auto-generated method stub

	}

	public static void main(String args[]) {
		SynchronizerGui synch = new SynchronizerGui();
		synch.setVisible(true);
	}
}
