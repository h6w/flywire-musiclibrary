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
import java.text.*;
import javax.media.*;
import javax.swing.*;
import javax.swing.border.*;


public class PlayList extends JPanel implements ControllerListener, MouseListener {
    public static final long serialVersionUID = 1L; //Why do we do this?

//        Vector list;
    PlayControl playcontrol;
    public PlayListMediaItem currentlyPlaying;
    JPanel viewPanel, bottomPanel;
    public JPanel list;
    JButton clearAllButton;
    JLabel digitalTimer;
    boolean sequence;
    JCheckBox cueModeCheckBox;

    public PlayList(PlayControl playcontrol, KeyListener k) {
        //list = new Vector();
        this.playcontrol = playcontrol;
        currentlyPlaying = null;
        sequence = false;

        list = new JPanel();
        list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));

        viewPanel = new JPanel(new BorderLayout());
        viewPanel.add(list,"North");
        viewPanel.add(new JPanel(),"Center");

        //Build the buttonPanel
        bottomPanel = new JPanel(new BorderLayout());
        JPanel leftPanel = new JPanel(new FlowLayout());
        clearAllButton = new JButton("Clear All");
        clearAllButton.setFont(new Font("Sans Serif",Font.PLAIN,15));
        clearAllButton.addMouseListener(this);
        leftPanel.add(clearAllButton);
        cueModeCheckBox = new JCheckBox("Cue Mode");
        cueModeCheckBox.setFont(new Font("Sans Serif",Font.PLAIN,15));
        cueModeCheckBox.setSelected(false);
        leftPanel.add(cueModeCheckBox);
        digitalTimer = playcontrol.getDigitalTimer();
        digitalTimer.setFont(new Font("Sans Serif",Font.BOLD,40));

        bottomPanel.add(leftPanel,"West");
        bottomPanel.add(digitalTimer,"East");



        JScrollPane scrollPane = new JScrollPane(viewPanel);

        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Playlist",
                       TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                       new Font("Sans Serif",Font.BOLD,20)));
        this.add(scrollPane,"Center");
        this.add(bottomPanel,"North");

        this.addKeyListener(k);
        viewPanel.addKeyListener(k);
        list.addKeyListener(k);
        bottomPanel.addKeyListener(k);
        clearAllButton.addKeyListener(k);
        cueModeCheckBox.addKeyListener(k);
    }

    /** Add a mediaitem to the playlist **/

    public int add(MediaItem mediaitem) {
        if (mediaitem == null) return(0);
            
        playcontrol.playable();
        PlayListMediaItem m = new PlayListMediaItem(this, mediaitem);
        Object[] mediaitems = list.getComponents();
        for (int i = 0; i < mediaitems.length; i++) {
                PlayListMediaItem inListMedia = (PlayListMediaItem)mediaitems[i];
                if (m.compareTo(inListMedia) == 0) return(0);
        }
        list.add(m);
        Player p = m.getPlayer();
        if (p == null) {
            list.remove(m);
            return(0);
        }
        p.addControllerListener(this);
        if (list.getComponentCount() == 1) currentlyPlaying = m;
        updateSelect();
        return(list.getComponentCount());
    }


    public boolean contains(MediaItem mediaItem) {
        Object[] mediaitems = list.getComponents();
        for (int i = 0; i < mediaitems.length; i++) {
            PlayListMediaItem plmi = (PlayListMediaItem)mediaitems[i];
            if (plmi.compareTo(mediaItem) == 0) return(true);
        }
        return(false);
    }


    /** Removes the specified mediaitem from the playlist **/
    void remove(PlayListMediaItem mediaitem) {
            int i = 0;
            ErrorHandler.info("Removing:"+mediaitem.getFilename());
            while (mediaitem != (PlayListMediaItem)list.getComponent(i)) i++;
            if (i >= list.getComponentCount()) {
                ErrorHandler.error("Trying to remove an item not in the list!!");
                return;
            }
            if (mediaitem == currentlyPlaying) {
                if (playcontrol.playingTrack() != null && currentlyPlaying.compareTo(playcontrol.playingTrack()) == 0) playcontrol.stop();
                if (list.getComponentCount() > i+1) currentlyPlaying = (PlayListMediaItem)list.getComponent(i+1);
                else if (list.getComponentCount() > 1 && i != 0) currentlyPlaying = (PlayListMediaItem)list.getComponent(0);
                else if (list.getComponentCount() > 1 && i == 0) currentlyPlaying = (PlayListMediaItem)list.getComponent(1);
                else currentlyPlaying = null;
            }
            list.remove(mediaitem);
            //mediaitem.destroy();
            if (list.getComponentCount() > 0) playcontrol.playable();
            else playcontrol.unplayable();
            updateSelect();
    }

    void removeMediaItem(MediaItem mediaitem) {
            Object[] mediaitems = list.getComponents();
            for (int i = 0; i < mediaitems.length; i++) {
                    PlayListMediaItem m = (PlayListMediaItem)mediaitems[i];
                    if (m.getMediaItem() == mediaitem) remove(m);
            }
            updateSelect();
    }

    public void removePlayedItems() {
        while (list.getComponentCount() > 0 && ((PlayListMediaItem)list.getComponent(0)) != currentlyPlaying) {
            PlayListMediaItem m = (PlayListMediaItem)list.getComponent(0);
            m.deselect();
            m.returnToSender(true);
        }
        updateSelect();
    }

    /** Clears the playlist **/

    public void clearAll() {
            Object[] mediaitems = list.getComponents();
            for (int i = 0; i < mediaitems.length; i++) {
                    PlayListMediaItem m = (PlayListMediaItem)mediaitems[i];
                    //m.clearNumber();
                    m.deselect();
                    m.returnToSender(false);
            }
            playcontrol.unplayable();
            updateSelect();
    }

    public int countItems() {
        return(list.getComponentCount());
    }


    /** Start playing the media in the playlist in order **/

    public void start() {
            //ErrorHandler.info("StartedEvent");
            if (list.getComponentCount() > 0) {
                ErrorHandler.info("Starting "+currentlyPlaying.getFilename());
                playcontrol.start(new Track(currentlyPlaying.getPlayer(),currentlyPlaying.getFilename()));
                playcontrol.stoppable();
            } else {
                ErrorHandler.error("Nothing in playlist!");
            }
    }

    public void stop() {
            //ErrorHandler.info("StoppedEvent");
            playcontrol.stop();
            if (list.getComponentCount() > 0) {
                    playcontrol.playable();
            } else playcontrol.unplayable();
    }

    /** Stop playing the media in the playlist and move to the next item in the playlist **/

    public PlayListMediaItem next() {
        for (int i = 0; i < list.getComponentCount(); i++) {
            if (list.getComponent(i) == currentlyPlaying) {
                if (i+1 >= list.getComponentCount()) return (null);
                else return((PlayListMediaItem)list.getComponent(i+1));
            }
        }
        return(null);
    }

    /** If not at the end of the playlist, and sequence is true,
            moves to the next item in the playlist and starts playing.
        Otherwise, stop playing.
        Called by playControl when the media has finished.
    **/

    public void EndOfMediaEvent(Controller player) {
        if (currentlyPlaying != null && player.equals(currentlyPlaying.getPlayer())) {
            //ErrorHandler.info("EndOfMediaEvent");
            ErrorHandler.info("Playlist moving on from "+currentlyPlaying.getFilename());
            currentlyPlaying = next();
            if (currentlyPlaying != null) {
                //ErrorHandler.error("currentlyPlaying != null, starting currentPlaying");
                start();
            } else {
                if (cueModeCheckBox.isSelected()) {
                    ErrorHandler.info("Running in cue mode, returning to beginning");
                    currentlyPlaying = (PlayListMediaItem)list.getComponent(0);
                    updateSelect();
                    cueModeCheckBox.setSelected(false);
                } else {
                    ErrorHandler.info("Returning "+list.getComponentCount()+" items");
                    while (list.getComponentCount() > 0) {
                            ErrorHandler.info("Getting the component.");
                            PlayListMediaItem plmi = (PlayListMediaItem)list.getComponent(0);
                            //m.clearNumber();
                            ErrorHandler.info("Deselecting it.");
                            plmi.deselect();
                            ErrorHandler.info("Returning it to the sender.");
                            plmi.returnToSender(true);
                            ErrorHandler.info(list.getComponentCount()+" items remaining.");
                    }
                    playcontrol.unplayable();
                }
            }
            if (list.getComponentCount() > 0) {
                    playcontrol.playable();
            } else {
                playcontrol.unplayable();
            }
        }
    }

    /** Set whether we want to continue playing after the media has finished. **/

    public void setSequence(boolean isSequence) {
            this.sequence = isSequence;
    }

    //public void notifyMediaAvailable() {
    //        playcontrol.mediaAvailable();
    //}

    public long getNextDuration() {
            if (list.getComponentCount() <= 0) return 0;
            return(next().getDuration().getNanoseconds());
    }


    /** Returns the total amount of time on the playlist in milliseconds **/
    public long getTotalTime() {
        long t = 0;
        int durationunknowncount = 0;
        for (int i = 0; i < list.getComponentCount(); i++) {
            PlayListMediaItem m = (PlayListMediaItem) list.getComponent(i);
            if (m != null) {
                while (m.getDuration() == Duration.DURATION_UNKNOWN && durationunknowncount < 20) {
                    try {
                        Thread.sleep(50);
                    }
                    catch (InterruptedException ie) {
                    }
                    durationunknowncount++;
                }
                try {
                    if (m.getDuration() != Duration.DURATION_UNKNOWN)
                        t += ((m.getDuration().getNanoseconds())/1000000);
                } catch (NullPointerException npe) {
                    t += 0;
                }
            }
        }
        return(t);
    }

    /** Returns the total amount of time on the playlist up to the current track in milliseconds **/
    public long getTimeSoFar() {
            long t = 0;
            int durationunknowncount = 0;
            for (int i = 0; i < list.getComponentCount() && list.getComponent(i) != currentlyPlaying; i++) {
                      PlayListMediaItem m = (PlayListMediaItem) list.getComponent(i);
                      while (m.getDuration() == Duration.DURATION_UNKNOWN && durationunknowncount < 20) {
                          try {
                              Thread.sleep(50);
                          }
                          catch (InterruptedException ie) {
                          }
                          durationunknowncount++;
                      }
                      if (m.getDuration() != Duration.DURATION_UNKNOWN)
                          t += ((m.getDuration().getNanoseconds())/1000000);
            }
            return(t);
    }

    public void mouseClicked(MouseEvent e) { if (e.getSource() == clearAllButton) this.clearAll(); }
    public void mouseEntered(MouseEvent e) { ; }
    public void mouseExited(MouseEvent e) { ; }
    public void mousePressed(MouseEvent e) { ; }
    public void mouseReleased(MouseEvent e) { ; }


    /** Updates the highlight to the currently playing mediaitem */
    public void updateSelect() {
        Object[] mediaitems = list.getComponents();
        for (int i = 0; i < mediaitems.length; i++) {
                PlayListMediaItem m = (PlayListMediaItem)mediaitems[i];
            if (currentlyPlaying == m)
                m.select();
            else m.deselect();
        }
    }


    public synchronized void controllerUpdate(ControllerEvent event) {
        //ErrorHandler.info("Got an event!");

        if (event instanceof TransitionEvent) {
            updateSelect();
        }
    }



class PlayListMediaItem extends JPanel implements MediaItemListener, ControllerListener, MouseListener {
    public static final long serialVersionUID = 1L; //Why do we do this?

    PlayList playlist;
    MediaItem mediaItem;
    JLabel lefttext;
    JLabel duration;

    public PlayListMediaItem(PlayList playlist, MediaItem mediaItem) {
        this.playlist = playlist;
        this.mediaItem = mediaItem;
        lefttext = new JLabel(mediaItem.getFilename());
        lefttext.setFont(new Font("Sans Serif",Font.PLAIN,15));
        duration = new JLabel("UNKNOWN");
        duration.setFont(new Font("Sans Serif",Font.PLAIN,15));

        this.setLayout(new BorderLayout());
        this.add(lefttext,"Center");
        this.add(duration,"East");

        if (mediaItem == null) {
            ErrorHandler.info("MediaItem was null when initialising PlayListMediaItem");
        } else {
            mediaItem.requestPlayer(this);
        }
        this.addMouseListener(this);
    }

    public void playerReady() {
        Player player = mediaItem.getPlayer();
        if (player != null) {
            player.addControllerListener(this);
            updateDuration();
        } else {
            ErrorHandler.info("No player was able to be created for the mediaitem \""+mediaItem.getFilename()+"\"");
        }        
    }

    public int compareTo(PlayListMediaItem m) { return(mediaItem.compareTo(m.getMediaItem())); }
    public int compareTo(MediaItem m) { return(mediaItem.compareTo(m)); }
    public int compareTo(Track t) { return(mediaItem.compareTo(t)); }
    public MediaItem getMediaItem() { return(mediaItem); }
    public void setNumber(int number) { mediaItem.setNumber(number); }
    public void clearNumber() { mediaItem.clearNumber(); }
    public Player getPlayer() { return(mediaItem.getPlayer()); }
    public Time getDuration() { return(mediaItem.getDuration()); }
    public String getFilename() { return(mediaItem.getFilename()); }

    void updateDuration() {
        if (mediaItem.getPlayer().getDuration() != Player.DURATION_UNKNOWN) {
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumIntegerDigits(2);
            nf.setMaximumIntegerDigits(2);
            nf.setMaximumFractionDigits(0);

            long nanosecond = 1000000000L;
            int seconds = (int)(((mediaItem.getPlayer().getDuration().getNanoseconds()) /      (nanosecond)) % 60);
            int minutes = (int)(((mediaItem.getPlayer().getDuration().getNanoseconds()) / (60 * nanosecond)) % 60);
            duration.setText(nf.format(minutes)+":"+nf.format(seconds));
        } else duration.setText("UNKNOWN");
    }

    public void select() {
        this.setBackground(Color.blue);
        lefttext.setForeground(Color.white);
        duration.setForeground(Color.white);
    }

    public void deselect() {
        Container c = this.getParent();
        if (c != null) this.setBackground(c.getBackground());
        lefttext.setForeground(Color.black);
        duration.setForeground(Color.black);
    }

    public void returnToSender(boolean wasPlayed) {
        if (wasPlayed) mediaItem.played();
        else mediaItem.restart();
        ErrorHandler.info("Removing "+mediaItem.getFilename());
        playlist.remove(this);
    }

    public void mouseClicked(MouseEvent e) {
        returnToSender(false);

    }
    public void mouseEntered(MouseEvent e) { ; }
    public void mouseExited(MouseEvent e) { ; }
    public void mousePressed(MouseEvent e) { ; }
    public void mouseReleased(MouseEvent e) { ; }

    /**
     * This controllerUpdate function must be defined in order to
     * implement a ControllerListener interface. This
     * function will be called whenever there is a media event
     */
    /** Prefetch the next media to be played, and set the time of all others to 0 **/
    public synchronized void controllerUpdate(ControllerEvent event) {
        //ErrorHandler.info("Got an event!");
        if (event instanceof RealizeCompleteEvent) {
            if (currentlyPlaying == this) {
                    this.select();
                    this.getPlayer().prefetch();
            }
            else {
                    this.getPlayer().setMediaTime(new Time(0));
                    this.deselect();
            }
        }

        Player p = mediaItem.getPlayer();
        if (p == null) {
            ErrorHandler.info("I got an event, but the mediaItem's player was null. Ignoring...");
            return;
        }
        
        if (p.getDuration() != Player.DURATION_UNKNOWN) {
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumIntegerDigits(2);
            nf.setMaximumIntegerDigits(2);
            nf.setMaximumFractionDigits(0);

            long nanosecond = 1000000000L;
            int seconds = (int)(((mediaItem.getPlayer().getDuration().getNanoseconds()) /      (nanosecond)) % 60);
            int minutes = (int)(((mediaItem.getPlayer().getDuration().getNanoseconds()) / (60 * nanosecond)) % 60);
            duration.setText(nf.format(minutes)+":"+nf.format(seconds));
        } else duration.setText("UNKNOWN");

    }

    public void destroy() {
       mediaItem.getPlayer().removeControllerListener(this);
       mediaItem.destroyPlayer();
    }
}

}
