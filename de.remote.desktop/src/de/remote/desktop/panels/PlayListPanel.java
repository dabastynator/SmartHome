package de.remote.desktop.panels;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.List;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IPlayList;
import de.remote.api.IPlayer;
import de.remote.api.PlayerException;

public class PlayListPanel extends Panel
{
  private List plsList;
  private List itemList;
  private IPlayList pls;
  private IPlayer player;
  private Map<String, String> itemMap = new HashMap();

  public PlayListPanel() {
    setName("Playlists");

    setLayout(new BorderLayout());
    add("Center", createLists());
    add("North", createButtons());
  }

  private Component createLists() {
    this.plsList = new List();
    this.plsList.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        PlayListPanel.this.updatePlsContent(PlayListPanel.this.plsList.getSelectedItem());
      }
    });
    this.itemList = new List();
    Panel lists = new Panel();
    lists.setLayout(new GridLayout());
    lists.add(this.plsList);
    lists.add(this.itemList);
    return lists;
  }

  private Component createButtons() {
    Button playPls = new Button("Play Playlist");
    playPls.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        try {
          PlayListPanel.this.player.playPlayList(PlayListPanel.this.plsList.getSelectedItem());
        } catch (RemoteException e1) {
          e1.printStackTrace();
        } catch (PlayerException e1) {
          e1.printStackTrace();
        }
      }
    });
    Button removePls = new Button("Delete PlayList");
    removePls.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        try {
          PlayListPanel.this.pls.removePlayList(PlayListPanel.this.plsList.getSelectedItem());
          PlayListPanel.this.updatePlsContent();
        } catch (RemoteException e1) {
          e1.printStackTrace();
        }
      }
    });
    Button createPls = new Button("Create new PlayList");
    createPls.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        try {
          Component source = (Component)e.getSource();
          String text = JOptionPane.showInputDialog(source, "Enter new name");
          if (text != null) {
            PlayListPanel.this.pls.addPlayList(text);
            PlayListPanel.this.updatePlsContent();
          }
        } catch (RemoteException e1) {
          e1.printStackTrace();
        }
      }
    });
    Button playItem = new Button("Play Item");
    playItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        try {
          PlayListPanel.this.player.play((String)PlayListPanel.this.itemMap.get(PlayListPanel.this.itemList.getSelectedItem()));
        } catch (RemoteException e1) {
          e1.printStackTrace();
        }
      }
    });
    Button removeItem = new Button("Remove item from Playlist");
    removeItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        String item = PlayListPanel.this.itemList.getSelectedItem();
        String plsName = PlayListPanel.this.plsList.getSelectedItem();
        if ((item != null) && (plsName != null) && (item.length() > 0) && 
          (plsName.length() > 0))
          try {
            PlayListPanel.this.pls.removeItem(plsName, item);
            PlayListPanel.this.updatePlsContent(plsName);
          } catch (RemoteException e1) {
            e1.printStackTrace();
          } catch (PlayerException e1) {
            e1.printStackTrace();
          }
      }
    });
    Panel buttons = new Panel();
    buttons.setLayout(new GridLayout(2, 3));
    buttons.add(playPls);
    buttons.add(createPls);
    buttons.add(playItem);
    buttons.add(removePls);
    buttons.add(removeItem);
    return buttons;
  }

  public void setPlayList(IPlayList pls) {
    this.pls = pls;
    updatePlsContent();
  }

  private void updatePlsContent() {
    this.plsList.removeAll();
    try {
      for (String p : this.pls.getPlayLists())
        this.plsList.add(p);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  private void updatePlsContent(String plsName) {
    this.itemList.removeAll();
    this.itemMap.clear();
    try {
      for (String item : this.pls.listContent(plsName)) {
        String sName = item.substring(item.lastIndexOf("/") + 1);
        this.itemMap.put(sName, item);
        this.itemList.add(sName);
      }
    } catch (RemoteException e) {
      e.printStackTrace();
    } catch (PlayerException e) {
      e.printStackTrace();
    }
  }

  public void setPlayer(IPlayer player) {
    this.player = player;
  }
}