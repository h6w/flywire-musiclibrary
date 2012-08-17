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

public class IDsPanel extends JPanel implements SelectionList {
    public static final long serialVersionUID = 1L; //Why do we do this?

    String IDsDirectory = "StationMedia";
    PlayControl playcontrol;
    PlayList playlist;
    JPanel viewPanel;
    JPanel list;
    //Vector list;
    ReloadIDsEvent e;
    TimedEventController t;
    Thread reloadThread;

    public IDsPanel(PlayList playlist, PlayControl playcontrol, KeyListener k) {
        this.playcontrol = playcontrol;
        this.playlist = playlist;

        list = new JPanel();
        list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));

        viewPanel = new JPanel();
        viewPanel.setLayout(new BorderLayout());
        viewPanel.add(list,"North");
        viewPanel.add(new JPanel(),"Center");

        JScrollPane scrollPane = new JScrollPane(viewPanel);
        loadIDs();

        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Station Media",
                       TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                       new Font("Sans Serif",Font.BOLD,20)));
        this.add(scrollPane,"Center");

        this.addKeyListener(k);
        viewPanel.addKeyListener(k);
        list.addKeyListener(k);

        // Update IDs every 5 mins
        e = new ReloadIDsEvent(this);
        t = new TimedEventController(e,Calendar.MINUTE,1);
        reloadThread = new Thread(t);
        reloadThread.start();

        File dir = new File(".",IDsDirectory);
        if (!dir.exists()) dir.mkdirs();
        if (!dir.isDirectory()) {
                JOptionPane.showMessageDialog(null, "You have a StationMedia file, but I was looking\nfor a StationMedia Directory.\nPlease accept my apologies.  I don't know what I should do here.  If you can think of what I should do, please contact the Flywire team.", "Alert", JOptionPane.ERROR_MESSAGE); 
                System.exit(-1);
        }
    }

    public void loadIDs() {
        File dir = new File(".",IDsDirectory);
        File[] media = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                boolean isDir = (new File(dir,name)).isDirectory();
                boolean isHidden = (new File(dir,name)).isHidden();
                return((!isDir) && (!isHidden));
            } 
        });

        ErrorHandler.info("Loading IDs from Dir:"+dir);
        if (media == null) ErrorHandler.error("No IDs!");
        else {          
            for (int i = 0; i < media.length; i++) {
                loadMedia(media[i]);
                //ErrorHandler.info("Loading "+media[i]);
            }
            sortList();
            list.revalidate();
        }
    }

    synchronized void loadMedia(File file) {
        //ErrorHandler.info("Loading media");
        //playcontrol.addMedia(filename);
        Object[] objs = list.getComponents();
        MediaItem[] mediaitems = new MediaItem[objs.length];
        for (int i = 0; i < objs.length; i++) mediaitems[i] = (MediaItem)objs[i];
        for (int i = 0; i < mediaitems.length; i++) {
                MediaItem inListMedia = (MediaItem)mediaitems[i];
                if (inListMedia.getFilename().compareTo(file.getName()) == 0) return;
        }
        list.add(new MediaItem(playcontrol, playlist, this, file, ""));
    }

    synchronized void sortList() {
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

    public MediaItem nextMediaItem() {
        int mediaNumber = Math.round((float)((float)Math.random()*((float)list.getComponentCount()-1)));
        
        return((MediaItem) list.getComponent(mediaNumber));
    }

}

class ReloadIDsEvent extends TimedEvent {
    IDsPanel p;
    public ReloadIDsEvent(IDsPanel p) {
        this.p = p;
    }

    public void doEvent() {
        p.loadIDs();
    }
}



