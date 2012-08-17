/*
    This file is part of Bentokit Flywire. http://bentokit.org/

    Flywire is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Flywire is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Flywire.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.bentokit;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;


public class ShowMediaPanel extends JPanel implements SelectionList {
    public static final long serialVersionUID = 1L; //Why do we do this?

    private File showMediaDir;
    ShowDropBox showdropbox;
    PlayControl playcontrol;
    PlayList playlist;
    JPanel viewPanel;
    JPanel list;
    ReloadShowsEvent e;
    TimedEventController t;
    Thread reloadThread;

    public ShowMediaPanel(PlayList playlist, PlayControl playcontrol, KeyListener k) {
            this.playcontrol = playcontrol;
            this.playlist = playlist;
            showMediaDir = new File("ShowMedia");
            if (!showMediaDir.exists()) showMediaDir.mkdirs();
            if (!showMediaDir.isDirectory()) {
                 JOptionPane.showMessageDialog(null, "You have a ShowMedia file, but I was looking\nfor a ShowMedia Directory.\nI cannot continue. Sorry.", "Alert", JOptionPane.ERROR_MESSAGE); 
                 System.exit(-1);
            }


            list = new JPanel();
            list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));

            showdropbox = new ShowDropBox(this,showMediaDir,playlist);
            showdropbox.setFont(new Font("Sans Serif",Font.PLAIN,15));
            showdropbox.loadlist();

            viewPanel = new JPanel();
            viewPanel.setLayout(new BorderLayout());
            viewPanel.add(list,"North");
            viewPanel.add(new JPanel(),"Center");
            JScrollPane scrollPane = new JScrollPane(viewPanel);

            this.setLayout(new BorderLayout());
            this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Show Media",
                       TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                       new Font("Sans Serif",Font.BOLD,20)));
            this.add(showdropbox,"North");
            this.add(scrollPane,"Center");

            this.addKeyListener(k);
            viewPanel.addKeyListener(k);
            list.addKeyListener(k);
            //showdropbox.addKeyListener(k);

            // Update the list of shows and media in shows every minute
            e = new ReloadShowsEvent(showdropbox);
            t = new TimedEventController(e,Calendar.MINUTE,1);
            reloadThread = new Thread(t);
            reloadThread.start();
    }

    public void loadShowMedia(String filename) {
            File dir = new File(showMediaDir,filename);
            File[] media = dir.listFiles();
            for (int i = 0; i < media.length; i++) {
                loadMedia(media[i]);
                //ErrorHandler.info("Loading "+media[i]);
            }
            this.sortShowMedia();
            list.revalidate();
    }

    void loadMedia(File file) {
            //ErrorHandler.info("Loading media");
            MediaItem m = new MediaItem(playcontrol, playlist, this, file, "");
            if (m == null) ErrorHandler.error("MediaItem is null!");
            //playcontrol.addMedia(filename);
            list.add(m);
    }

    public void emptyList() {
            //ErrorHandler.info("Emptying List");
            if (list != null) {
                    Component[] media = list.getComponents();
                    for (int i = 0; i < media.length; i++) {
                            MediaItem m = (MediaItem) media[i];
                            playlist.removeMediaItem(m);
                            m.destroy();
                    }
                    list.removeAll();
            }
            //ErrorHandler.info("Emptied List");
    }

    synchronized void sortShowMedia() {
        Object[] objs = list.getComponents();
        MediaItem[] mediaitems = new MediaItem[objs.length];
        for (int i = 0; i < objs.length; i++) mediaitems[i] = (MediaItem)objs[i];
        list.removeAll();
        java.util.Arrays.sort(mediaitems,new MediaItemComparator());
        for (int i = 0; i < mediaitems.length; i++) {
            list.add((MediaItem)mediaitems[i]);
        }
    }



    public void played(MediaItem m) { ; } // For selectionList
    public void remove(MediaItem m) { list.remove(m); } // For selectionList
}


class ShowDropBox extends JComboBox implements ItemListener {
    public static final long serialVersionUID = 1L; //Why do we do this?

    String nowselected;
    File showMediaDir;
    ShowMediaPanel panel;
    PlayList playlist;

    public ShowDropBox(ShowMediaPanel panel, File showMediaDir, PlayList playlist) {
        this.panel = panel;
        nowselected = null;
        //this.setEditable(false);
        this.showMediaDir = showMediaDir;
        this.playlist = playlist;
        this.addItemListener(this);
    }

    public void processKeyEressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_P) {
            playlist.setSequence(true);
            playlist.start();
        }
    }

    public boolean selectWithKeyChar(char keyChar) {
        if (keyChar == 'p' || keyChar == 'P') {
            playlist.setSequence(true);
            playlist.start();
        }
        return(true);
    }

    public boolean isInList(String show) {
        for (int i = 0; i < this.getItemCount(); i++) {
            if (((String)this.getItemAt(i)).compareTo(show) == 0) return(true);
        }
        return(false);
    }

    public boolean isInArray(String needle, String[] haystack) {
        for (int i = 0; i < haystack.length; i++) {
            if (haystack[i].compareTo(needle) == 0) return(true);
        }
        return(false);
    }

    public void loadlist() {
            String[] shows = showMediaDir.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    boolean isDir = (new File(dir,name)).isDirectory();
                    boolean isHidden = (new File(dir,name)).isHidden();
                    return(isDir && !(isHidden));
                }
            });
            //add new shows
            for (int i = 0; i < shows.length; i++) {
                //ErrorHandler.info("Checking against: "+shows[i]);
                    if (!isInList(shows[i])) {
                        //ErrorHandler.info("Adding: "+shows[i]);
                        this.addItem(shows[i]);
                    }
            }
            //remove old shows
            for (int i = 0; i < this.getItemCount(); i++) {
                if (!isInArray((String)this.getItemAt(i),shows)) {
                    //ErrorHandler.info("Removing: "+((String)this.getItemAt(i)));
                    this.removeItemAt(i);
                }
            }
            if (shows.length <= 0) {
                this.addItem("No Shows Loaded");
            }
        }

        public void itemStateChanged(ItemEvent e) {
                String newselection = (String) this.getSelectedItem();
                ErrorHandler.info("ItemStateChange to "+newselection);

                if (newselection == null) ErrorHandler.error("newselection is null");
                if (nowselected == null) ErrorHandler.error("nowselected is null");

                if (newselection != null && (newselection.compareTo("No Shows Loaded") != 0)) {
                        ErrorHandler.info("newselection is not null");
                        if (nowselected == null)        {
                                ErrorHandler.info("Combo box value changing to "+newselection);
                                nowselected = newselection;
                                panel.emptyList();
                                panel.loadShowMedia(nowselected);
                                ErrorHandler.info("New Media Loaded");
                        } else if (nowselected.compareTo(newselection) != 0) {
                                ErrorHandler.info("Combo box value changing to "+newselection);
                                nowselected = newselection;
                                panel.emptyList();
                                panel.loadShowMedia(nowselected);
                                ErrorHandler.info("New Media Loaded");
                        } else {
                                ErrorHandler.error("return value from compare="+nowselected.compareTo(newselection));
                                ErrorHandler.error("nowselected:"+nowselected);
                                ErrorHandler.error("newselection:"+newselection);
                        }
                }
        }

}

class ReloadShowsEvent extends TimedEvent {
    public static final long serialVersionUID = 1L; //Why do we do this?

    ShowDropBox p;
    public ReloadShowsEvent(ShowDropBox p) {
        this.p = p;
    }

    public void doEvent() {
        p.loadlist();
    }
}
