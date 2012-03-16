package de.remote.desktop.menus;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import de.remote.desktop.ControlFrame;

public class ServerMenu extends Menu
{
  private ControlFrame mainFrame;

  public ServerMenu(ControlFrame mainFrame)
  {
    super("Server");
    this.mainFrame = mainFrame;
    for (String name : ControlFrame.serverList.keySet()) {
      MenuItem item = new MenuItem(name);
      item.addActionListener(new ServerActionListener(name));
      add(item);
    }
  }

  public class ServerActionListener implements ActionListener {
    private String ip;

    public ServerActionListener(String server) {
      this.ip = ((String)ControlFrame.serverList.get(server));
    }

    public void actionPerformed(ActionEvent e)
    {
      ServerMenu.this.mainFrame.connectToServer(this.ip);
    }
  }
}