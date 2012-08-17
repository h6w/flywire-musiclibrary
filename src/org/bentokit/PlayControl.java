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
import javax.swing.*;
import java.lang.String;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.io.IOException;
import java.text.*;
import javax.media.*;

public class PlayControl extends JPanel implements ControllerListener, ActionListener, Runnable {
    public static final long serialVersionUID = 1L; //Why do we do this?

    //Sound hashtable
    //SoundList soundList;
    //The currently chosen file
    URL chosenFile;
    //The URL of the program file
    URL codeBase;

    // timer Thread
    protected Thread timeThread = null;

    // Current Player
    Track currenttrack;

    // PlayList
    PlayList playlist;

    //Automatic DJ
    AutomaticDJ autodj;

    JButton playButton, stopButton, playallButton;
    JSlider progressBar;
    JLabel digitalTimer;
    int timex;

    /**
     * Create the media player.
     */
    public PlayControl(KeyListener k) {
        try {
            File baseFile = new File(".");
            codeBase = baseFile.toURI().toURL();
        } catch (MalformedURLException e) {
            ErrorHandler.error(e.getMessage());
        }

        playButton = new JButton("Play One");
        playButton.addActionListener(this);

        playallButton = new JButton("", new ImageIcon("bin"+File.separator+"play.png"));
        playallButton.setDisabledIcon(new ImageIcon("bin"+File.separator+"playdisabled.png"));
        playallButton.setBorderPainted(false);
        playallButton.setContentAreaFilled(false);
        playallButton.addKeyListener(k);
        //playallButton.setMnemonic(KeyEvent.VK_P);
        playallButton.addActionListener(this);

        stopButton = new JButton("", new ImageIcon("bin"+File.separator+"stop.png"));
        stopButton.setDisabledIcon(new ImageIcon("bin"+File.separator+"stopdisabled.png"));
        stopButton.addKeyListener(k);
        stopButton.setBorderPainted(false);
        stopButton.setContentAreaFilled(false);
        stopButton.addActionListener(this);

        progressBar = new JSlider(JSlider.HORIZONTAL,0,1000,0);
        progressBar.setEnabled(false);

        digitalTimer = new JLabel("00:00:00");
        digitalTimer.setFont(new Font("Sans Serif",Font.BOLD,20));

        unplayable();

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(playallButton);
        buttonPanel.add(stopButton);

                this.setLayout(new BorderLayout());

        //this.add(playButton);
        this.add(buttonPanel,"West");
        this.add(progressBar,"Center");
        this.setVisible(true);

        currenttrack = null;

        // Create the slider update thread
        timeThread = new Thread(this);
        timeThread.start();
    }

    public void setPlayList(PlayList playlist) {
                this.playlist = playlist;
    }

    public void setAutomaticDJ(AutomaticDJ autodj) {
                this.autodj = autodj;
    }


    public JLabel getDigitalTimer() {
        return(digitalTimer);
    }


    //void addMedia(String filename) {
        //        soundList.startLoading(filename);
        //}

    public Player getPlayer(String filename) {
        //ErrorHandler.info("Getting a player");
        MediaLocator mrl = null;
        Player player = null;

        try {
            chosenFile = (new File(filename)).toURI().toURL();
            //ErrorHandler.info("chosenFileURL:"+chosenFile);
        } catch (MalformedURLException e){
            ErrorHandler.error(e.getMessage());
        }
        try {
            // Create a media locator from the file name
            if ((mrl = new MediaLocator(chosenFile)) == null)
                ErrorHandler.fatal("Can't build URL for " + chosenFile,-1);

            // Create an instance of a player for this media
            try {
                player = Manager.createPlayer(mrl);
            } catch (NoPlayerException e) {
                ErrorHandler.error(e);
                ErrorHandler.error("Could not create player for \"" + mrl.getURL().toString() + "\" because:"+e.getMessage());
            }

        } catch (MalformedURLException e) {
            ErrorHandler.fatal("Invalid media file URL!",-1);
        } catch (IOException e) {
            ErrorHandler.fatal("IO exception creating player for " + mrl,-1);
        }
        
        if (player != null) player.prefetch();

        return(player);
        }


        //void removeMedia(String filename) {
        //        soundList.remove(filename);
        //}

    /**
     * Start media file playback. This function is called the
     * first time that the Applet runs and every
     * time the user re-enters the page.
     */

    public void start(Track track) {
        if (currenttrack != null) {
            currenttrack.getPlayer().removeControllerListener(this);
            ErrorHandler.info("Removed controllerListener from "+currenttrack.getName());
        }
        currenttrack = track;
        currenttrack.getPlayer().addControllerListener(this);
        ErrorHandler.info("Added controllerListener to "+currenttrack.getName());

        currenttrack.getPlayer().setMediaTime(new Time(0));
        currenttrack.getPlayer().start();

        // Add ourselves as a listener for a player's events
    }


    /**
     * Returns the track currently playing or null if no track is playing.
     */

    public Track playingTrack() {
        return(currenttrack);
    }
    /**
     * Stop media file playback and release resource before
     * leaving the page.
     */
    public void stop() {
        ErrorHandler.info("Stopping "+currenttrack.getName());
        if (currenttrack != null) {
            currenttrack.getPlayer().stop();
        //    player.deallocate();
        }
                currenttrack = null;
    }

        public void unplayable() {
                playButton.setEnabled(false);
                playallButton.setEnabled(false);
                stopButton.setEnabled(true);
                digitalTimer.setText("00:00:00");
        }

        public void playable() {
                playButton.setEnabled(true);
                playallButton.setEnabled(true);
                stopButton.setEnabled(true);
        }

        public void stoppable() {
                playButton.setEnabled(false);
                playallButton.setEnabled(true);
                stopButton.setEnabled(true);
        }

    /**
     * This controllerUpdate function must be defined in order to
     * implement a ControllerListener interface. This
     * function will be called whenever there is a media event
     */
    public synchronized void controllerUpdate(ControllerEvent event) {
                ErrorHandler.info("An event");
                Controller c = event.getSourceController();
                if (event instanceof EndOfMediaEvent) {
                        //ErrorHandler.info("EOM event");
                        currenttrack = null;
                        ErrorHandler.info(MediaItem.getFilenameFromPlayer(c)+":Sending EOM event to playlist");
                        playlist.EndOfMediaEvent(c);
                        ErrorHandler.info(MediaItem.getFilenameFromPlayer(c)+":Sending EOM event to autodj");
                        autodj.EndOfMediaEvent(c);
                } else if (event instanceof MediaTimeSetEvent) {
                    ErrorHandler.info(MediaItem.getFilenameFromPlayer(c)+":A media time event: "+event.toString());
                } else if (event instanceof ControllerErrorEvent) {
                        // Tell TypicalPlayerApplet.start() to call it a day
                        ErrorHandler.fatal(MediaItem.getFilenameFromPlayer(c)+":"+((ControllerErrorEvent)event).getMessage(),-1);
                } else if (event instanceof TransitionEvent) {
                            // Tell TypicalPlayerApplet.start() to call it a day
                            //Player finishedPlayer = (Player) event.getSource();
                            TransitionEvent e = (TransitionEvent) event;
                            //if (e.getPreviousState() == e.getTargetState()) {
                                String previousStateStr, currentStateStr, targetStateStr;

                                switch(e.getPreviousState()) {
                                    case Controller.Prefetched:
                                        previousStateStr = "Prefetched"; break;
                                    case Controller.Prefetching:
                                        previousStateStr = "Prefetching"; break;
                                    case Controller.Realized:
                                        previousStateStr = "Realized"; break;
                                    case Controller.Started:
                                        previousStateStr = "Started"; break;
                                    case Controller.Unrealized:
                                        previousStateStr = "Unrealized"; break;
                                    default:
                                        previousStateStr = "Unknown"; break;
                                }

                                switch(e.getCurrentState()) {
                                    case Controller.Prefetched:
                                        currentStateStr = "Prefetched"; break;
                                    case Controller.Prefetching:
                                        currentStateStr = "Prefetching"; break;
                                    case Controller.Realized:
                                        currentStateStr = "Realized"; break;
                                    case Controller.Started:
                                        currentStateStr = "Started"; break;
                                    case Controller.Unrealized:
                                        currentStateStr = "Unrealized"; break;
                                    default:
                                        currentStateStr = "Unknown"; break;
                                }

                                switch(e.getTargetState()) {
                                    case Controller.Prefetched:
                                        targetStateStr = "Prefetched"; break;
                                    case Controller.Prefetching:
                                        targetStateStr = "Prefetching"; break;
                                    case Controller.Realized:
                                        targetStateStr = "Realized"; break;
                                    case Controller.Started:
                                        targetStateStr = "Started"; break;
                                    case Controller.Unrealized:
                                        targetStateStr = "Unrealized"; break;
                                    default:
                                        targetStateStr = "Unknown"; break;
                                }
                                ErrorHandler.info(MediaItem.getFilenameFromPlayer(c)+":"+previousStateStr+"->"+currentStateStr+"=>"+targetStateStr);
                            //}
                    } else if (event instanceof StopEvent) {
                            //StopEvent e = (StopEvent) event;
                            ErrorHandler.info(MediaItem.getFilenameFromPlayer(c)+":->Stopped");
                    } else if (event instanceof DeallocateEvent) {
                            //DeallocateEvent e = (DeallocateEvent) event;
                            ErrorHandler.info(MediaItem.getFilenameFromPlayer(c)+":->Deallocate");
                    } else if (event instanceof ControllerErrorEvent) {
                            // Tell TypicalPlayerApplet.start() to call it a day
                            ErrorHandler.fatal(MediaItem.getFilenameFromPlayer(c)+":"+((ControllerErrorEvent)event).getMessage(),-1);
                    } else if (event instanceof DurationUpdateEvent) {
                            // Tell TypicalPlayerApplet.start() to call it a day
                            ErrorHandler.info(MediaItem.getFilenameFromPlayer(c)+":A duration update event");
                    } else {
                        ErrorHandler.error(MediaItem.getFilenameFromPlayer(c)+":Unknown Event:"+event.toString());

                    }

    }

    public void actionPerformed(ActionEvent event) {
                Object source = event.getSource();

                //PLAY BUTTON
                if (source == playButton) {
                        playlist.setSequence(false);
                        //Try to get the AudioClip.
                        playlist.start();
                        return;
                }

                //PLAY ALL BUTTON
                if (source == playallButton) {
                        playlist.setSequence(true);
                        //Try to get the AudioClip.
                        playlist.start();
                        return;
                }

                //STOP BUTTON
                if (source == stopButton) {
                        playlist.stop();
                        return;
                }

        }

        /**
         * The media time slider update thread
         */
        public void run() {
                while (true) {
                        long sofar, total;
                        sofar = total = 0;
                        if (playlist != null) {
                                total = playlist.getTotalTime();
                                sofar = playlist.getTimeSoFar();
                        }
                        if (currenttrack != null) {
                                //if (currentplayer.getState() == Player.Started) {
                                        sofar += (currenttrack.getPlayer().getMediaTime().getNanoseconds()/1000000);
                        }

                        if (total >= 0 && total < (long) 3 * 3600 * 1000000000L) {
                                if (progressBar != null) {
                                        timex = (int) (((float) sofar / total) * progressBar.getMaximum());
                                        progressBar.setValue(timex);
                                }
                                long remaining = total - sofar;
                                NumberFormat nf = NumberFormat.getInstance();
                                nf.setMinimumIntegerDigits(2);
                                nf.setMaximumIntegerDigits(2);
                                nf.setMaximumFractionDigits(0);
                                long millisecond = 1000L;
                                int seconds =    (int)(((remaining) /        (millisecond)) % 60);
                                int minutes =    (int)(((remaining) /   (60 * millisecond)) % 60);
                                int hours =      (int)(((remaining) / (3600 * millisecond)) % 60);

                                digitalTimer.setText(nf.format(hours)+":"+nf.format(minutes)+":"+nf.format(seconds));
                        } else {
                                digitalTimer.setText("UNKNOWN");
                        }


                        try {
                                Thread.sleep(50);
                        }
                        catch (InterruptedException ie) {
                        }
                }
        }


}
