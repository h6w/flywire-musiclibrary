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
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.media.*;

public class MediaItem extends JPanel implements MouseListener, ControllerListener {
    public static final long serialVersionUID = 1L; //Why do we do this?

    //A media Item Queue, so we don't overload the JMF
    public static final int maxMediaItems = 10;
    public static volatile Vector<MediaItem> allMediaItems = new Vector<MediaItem>();
    public static volatile Vector<MediaItem> runningMediaItems = new Vector<MediaItem>();
    public static volatile Vector<MediaItem> mediaItemQueue = new Vector<MediaItem>();

    public static volatile WatchRunningEvent running_watch;
    public static volatile TimedEventController running_watch_eventcontroller;
    public static volatile Thread watchThread;

    public static volatile ReviewUnknownEvent unknown_watch;
    public static volatile TimedEventController unknown_watch_eventcontroller;
    public static volatile Thread unknownThread;


    PlayControl playcontrol;
    protected PlayList playlist;
    protected SelectionList selectionList;
    File file;
    String description;
    JPanel eastPanel;
    JLabel lefttext;
    JLabel durationLabel;
    Time duration;
    //int durationAttempt;
    JLabel spacer;
    JLabel number;
    protected Player player;
    Vector<MediaItem> children;

    ArrayList<MediaItemListener> listeners;

    //These all watch for filesystem changes to the underlying file.
    long fileLastModified;  //To keep an eye on any changes to the file
    long fileLength; //To keep an eye on any changes to the file
    ReloadMediaItemEvent e;
    TimedEventController t;
    private volatile Thread reloadThread;

    public MediaItem(PlayControl playcontrol, PlayList playlist, SelectionList selectionList) {
        this.playcontrol = playcontrol;
        this.playlist = playlist;
        this.selectionList = selectionList;

        MediaItem.allMediaItems.add(this);

/*
        if (watchThread == null) {
            //Check for loser tracks every 3 seconds (tracks that aren't giving us a player)
            running_watch = new WatchRunningEvent();
            running_watch_eventcontroller = new TimedEventController(running_watch,Calendar.SECOND,2);
            watchThread = new Thread(running_watch_eventcontroller);
            watchThread.start();
        }

        if (unknownThread == null) {
            //Check for unknown tracks every second (tracks that don't have a time yet)
            unknown_watch = new ReviewUnknownEvent();
            unknown_watch_eventcontroller = new TimedEventController(unknown_watch,Calendar.SECOND,6);
            unknownThread = new Thread(unknown_watch_eventcontroller);
            unknownThread.start();
        }
*/

        lefttext = new JLabel("");
        lefttext.setFont(new Font("Sans Serif",Font.PLAIN,15));
        number = new JLabel("");
        number.setFont(new Font("Sans Serif",Font.PLAIN,15));
        durationLabel = new JLabel("UNKNOWN");
        durationLabel.setFont(new Font("Sans Serif",Font.PLAIN,15));

        this.setLayout(new BorderLayout());
        this.add(lefttext,"Center");
        this.add(number,"East");

        spacer = new JLabel("     ");

        eastPanel = new JPanel(new BorderLayout());

        eastPanel.add(durationLabel,"West");
        //eastPanel.add(spacer,"Center");
        //eastPanel.add(number,"East");

        this.setLayout(new BorderLayout());
        this.add(lefttext,"Center");
        this.add(eastPanel,"East");
        this.setSelectable(true);

        listeners = new ArrayList<MediaItemListener>();

        this.addMouseListener(this);
    }


    public MediaItem(PlayControl playcontrol, PlayList playlist, SelectionList selectionList, File file, String description) {
        this(playcontrol,playlist,selectionList);

        //ErrorHandler.info("Creating MediaItem");
        //ErrorHandler.info("#1");
        this.setFile(file);
        this.description = description;

        lefttext.setText(file.getName());
        //ErrorHandler.info("#2");

        //ErrorHandler.info("Name:"+file.getName());
        //ErrorHandler.info("Path:"+file.getPath());
        //try {
        //    ErrorHandler.info("Canonical Path:"+file.getCanonicalPath());
        //} catch (IOException e) {
        //  ErrorHandler.error(e);
        //}
        //
        //ErrorHandler.info("Absolute Path:"+file.getAbsolutePath());

        this.createPlayer();

        //ErrorHandler.info("Only GUI stuff left for this item");
        //ErrorHandler.info("Created MediaItem");

        children = new Vector<MediaItem>();
    }

    public Object clone() {
        return((Object)new MediaItem(playcontrol,playlist,selectionList,file,description));
    }

    public void addChild(MediaItem m) {
        children.add(m);
    }

    public int compareTo(MediaItem m) {
        return(this.getFilename().compareTo(m.getFilename()));
    }

    public int compareTo(Track t) {
        return(this.getFilename().compareTo(t.getName()));
    }

    public void requestPlayer(MediaItemListener listener) {
        if (this.player != null) listener.playerReady();
        else {
            listeners.add(listener);
            this.createPlayer();
        }        
    }

    public synchronized void createPlayer() {
        if ((!this.isInPlaylist()) && MediaItem.runningMediaItems.size() >= MediaItem.maxMediaItems) {
            ErrorHandler.info("Too many media items, adding to the queue: "+file.getPath());
            if (!MediaItem.mediaItemQueue.contains(this))       
                MediaItem.mediaItemQueue.add(this);
        } else {
            ErrorHandler.info("Removing from queue, creating player and adding to running list: "+file.getPath());       
            MediaItem.mediaItemQueue.remove(this);
            ErrorHandler.info("Creating player for file "+file.getPath());       
            player = playcontrol.getPlayer(file.getPath());
            if (player != null) {
                player.addControllerListener(this);
                MediaItem.runningMediaItems.add(this);
                for (MediaItemListener listener : listeners) listener.playerReady();
            }
        }
    }

    public synchronized void destroyPlayer() {
        MediaItem.runningMediaItems.remove(this);
        ErrorHandler.info("Destroying player for file "+file.getPath());       
        if (player != null) player.close();
        player = null;
        if (MediaItem.mediaItemQueue.size() > 0) {
            MediaItem.mediaItemQueue.get(0).createPlayer();
        }
    }


    public MediaItem(PlayControl playcontrol, PlayList playlist, SelectionList selectionList, String filename, String description) {
        this(playcontrol,playlist,selectionList);

        ErrorHandler.info("Creating MediaItem");
        this.file = new File(filename);
        this.description = description;

        player = playcontrol.getPlayer(filename);

        //ErrorHandler.info("Created MediaItem");
    }

    public void setFile(File file) {
        this.file = file;
        this.fileLastModified = file.lastModified();
        this.fileLength = file.length();

        // Update IDs every 5 mins
        e = new ReloadMediaItemEvent(this);
        t = new TimedEventController(e,Calendar.SECOND,10);
        reloadThread = new Thread(t);
        reloadThread.start();

        this.createPlayer();
    }

    /** Private method for subclasses **/
    public void setFilename(String filename) {
        this.setFile(new File(filename));
    }


    public void setNumber(int newnumber) {
        StringBuffer numStr = new StringBuffer(String.valueOf(newnumber));
        for (int i = numStr.length(); i <= 3; i++)
            numStr = numStr.insert(0," ");
        number.setText(new String(numStr));
    }

    public void clearNumber() {
        number.setText("   ");
    }

    public Player getPlayer() {
        //if (player == null) this.createPlayer();
        //GetPlayer no longer creates a player because that causes extraneous players.
        //The queue will stop us from creating players if we don't need to.
        return(player);
    }

    public String getFilename() {
        return(file.getName());
    }

    public void setDuration(Time duration) {
        this.duration = duration;

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);

        long nanosecond = 1000000000L;

        int seconds = (int)(((this.duration.getNanoseconds()) /      (nanosecond)) % 100);
        int minutes = (int)(((this.duration.getNanoseconds()) / (60 * nanosecond)) % 100);
        durationLabel.setText(nf.format(minutes)+":"+nf.format(seconds));                    
    }

    public Time getDuration() {
        return(this.duration);
    }

    public void setText(String text) {
        if (lefttext == null) {
            lefttext = new JLabel();
            
        }
        lefttext.setText(text);
    }

    public void setSelectable(boolean isSelectable) {
        if (isSelectable) this.addMouseListener(this);
        else this.removeMouseListener(this);
    }

    public void remove() {
        playlist.remove(this);
        selectionList.remove(this);
    }

    public synchronized void addToPlaylist() {
        //playlist.add(this);
//      if (number.getText().compareTo("   ") == 0) {
            //Get the media ready to play
            number.setText(String.valueOf(playlist.add(this)));
            if (this.player == null) this.createPlayer();
//      }
//      else {
//          playlist.remove(this);
//          number.setText("   ");
//      }
    }

    public boolean isInPlaylist() {
        return(playlist.contains(this));
    }


    //private MediaItem getThis() { return(this); }

    public void mouseClicked(MouseEvent e) { this.addToPlaylist(); }
    public void mouseEntered(MouseEvent e) { ; }
    public void mouseExited(MouseEvent e) { ; }
    public void mousePressed(MouseEvent e) { ; }
    public void mouseReleased(MouseEvent e) { ; }

    /**
       Our controllerUpdate basically is listening for
       the signal to say the JMF has figured out the
       length of the media.   Once it has done that,
       and presuming it hasn't been added to the playlist
       in the meantime, the player is destroyed to preserve
       memory.

       \TODO The problem with this is, that for media for which
       the JMF cannot determine the length, that media will be locked
       forever.  We need to set up a timeout thread that says after
       some time or signal we should give up.

       The JMF appears a little unstable at times trying to calculate
       media length.  We may even have to close and reopen the player
       a few times to be sure.
    */
    public synchronized void controllerUpdate(ControllerEvent e) {
        if (this.duration == null) {
            if (player.getDuration() != Player.DURATION_UNKNOWN) {
                this.setDuration(player.getDuration());

                //If we're not about to play it, release it
                if (!this.isInPlaylist()) {
                    this.destroyPlayer();
                }
            } else durationLabel.setText("UNKNOWN");
        }
    }

    public synchronized void destroy() {
        MediaItem.allMediaItems.remove(this);
        if (children != null) {
            Object[] elements = children.toArray();
            for (int i = 0; i < elements.length; i++) {
                MediaItem m = (MediaItem) elements[i];
                m.remove();
            }
        }
        this.destroyPlayer();
        this.reloadThread = null;
    }

    /** A function for post-playing operations, this one simply resets the player **/
    public synchronized void played() { player.setMediaTime(new Time(0)); }

    /** A function for restarting the media **/
    public synchronized void restart() { 
        if (player == null) { this.createPlayer(); }
        player.setMediaTime(new Time(0)); 
    }

    public synchronized void checkForChanges() {
        //Check to see if the file has disappeared
        if (!file.exists()) {
           //If it has, remove it from everything and destroy
           playlist.removeMediaItem(this);
           selectionList.remove(this);
           this.destroy();
           return;
        }
        //Otherwise, check to see if the file has changed
        if (file.lastModified() != this.fileLastModified 
            || file.length() != this.fileLength) {
            this.destroyPlayer();                          
            this.duration = null;
            this.fileLastModified = file.lastModified();
            this.fileLength = file.length();    
            this.createPlayer();
        }
    }

    public synchronized static String getFilenameFromPlayer(Controller player) {
        for (MediaItem item : allMediaItems) {
            if (item.getPlayer() != null && item.getPlayer() == player)
                return(item.getFilename());
        }
        return("Unknown MediaItem");
    }
}

class ReloadMediaItemEvent extends TimedEvent {
    MediaItem m;
    public ReloadMediaItemEvent(MediaItem m) {
        this.m = m;
    }

    public void doEvent() {
        m.checkForChanges();
    }
}

class MediaItemComparator implements java.util.Comparator<MediaItem> {
    public int compare(MediaItem o1, MediaItem o2) {
        String str1 = o1.getFilename();
        return(str1.compareToIgnoreCase(o2.getFilename()));
    }
   
    public boolean equals(MediaItemComparator o) {
        //There is only one MediaItemComparator
        return(true);
    }
}

class WatchRunningEvent extends TimedEvent {
    public static final int maxTime = 10; //Maximum time to allow a player to live if it's not in the playlist.
    private ConcurrentHashMap<MediaItem,Integer> timeRunning;

    public WatchRunningEvent() { 
        this.timeRunning = new ConcurrentHashMap<MediaItem,Integer>(); 
    }

    public void doEvent() {
        try {
            ErrorHandler.info("Checking for zombie MediaItems via event");
            for (MediaItem item : MediaItem.runningMediaItems) {
                if (this.timeRunning.containsKey(item)) {
                    Integer i = this.timeRunning.get(item);
                    i = new Integer(i.intValue()+1);
                    this.timeRunning.put(item,i);
                }
                else this.timeRunning.put(item,0);
                if (this.timeRunning.get(item) > maxTime && !item.isInPlaylist()) {
                    ErrorHandler.info("Nonresponsive item, and not in playlist, giving up: "+item.getFilename());                  
                    item.destroyPlayer();
                }
            }
            
            for (MediaItem item : this.timeRunning.keySet()) {
                if (!MediaItem.runningMediaItems.contains(item))
                    this.timeRunning.remove(item);
            }
        } catch (ConcurrentModificationException cme) {
            ErrorHandler.info("Running Items Concurrently Modified. Failing silently.");
        }
    }
}


class ReviewUnknownEvent extends TimedEvent {
    public synchronized void doEvent() {
        ErrorHandler.info("Checking for unknown MediaItems via event");
        for (MediaItem item : MediaItem.allMediaItems) {
            if (item.getDuration() == null) {
                item.createPlayer();
            }
        }
    }
}
