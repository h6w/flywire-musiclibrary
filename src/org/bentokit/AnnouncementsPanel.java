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
import javax.swing.*;
import javax.swing.border.*;


public class AnnouncementsPanel extends JPanel implements SelectionList, MouseListener, Runnable {
    public static final long serialVersionUID = 1L; //Why do we do this?

    static final String ScheduleMediaDir = "ScheduledMedia";
    static final String ScheduleDir = "Schedule";
    static final String LogDir = "Log";
    PlayControl playcontrol;
    PlayList playlist;
    JPanel timePanel;
    JLabel timeLabel;
    JPanel viewPanel;
    JPanel list;
    JButton addAllButton;
    Calendar nextAnnouncementTime;
    SimpleDateFormat parsedateformat;
    SimpleDateFormat printtimeformat;
    SimpleDateFormat printdatetimeformat;
    SimpleDateFormat printdateformat;

    ReloadAnnouncementsEvent reload_event;
    TimedEventController reload_eventcontroller;
    Thread reloadThread;
    AnnouncementTimeEvent announcement_event;
    TimedEventController announcement_eventcontroller;
    private volatile Thread announcementThread;

    private volatile Thread flashThread;


    public AnnouncementsPanel(PlayList playlist, PlayControl playcontrol, KeyListener k) {
        this.playcontrol = playcontrol;
        this.playlist = playlist;
        nextAnnouncementTime = Calendar.getInstance();
        ErrorHandler.info("NextTime:"+nextAnnouncementTime.getTime());

        timePanel = new JPanel(new BorderLayout());

        timeLabel = new JLabel("00:00");
        timeLabel.setFont(new Font("Sans Serif",Font.BOLD,25));

        addAllButton = new JButton("Add All");
        addAllButton.setFont(new Font("Sans Serif",Font.PLAIN,15));
        addAllButton.addMouseListener(this);
        JPanel addAllButtonPanel = new JPanel();
        addAllButtonPanel.add(addAllButton);

        timePanel.add(timeLabel,"Center");
        timePanel.add(addAllButtonPanel,"East");

        list = new JPanel();
        list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));


        viewPanel = new JPanel(new BorderLayout());
        viewPanel.add(list,"North");
        viewPanel.add(new JPanel(),"Center");

        JScrollPane scrollPane = new JScrollPane(viewPanel);

        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Sponsorship Announcements",
                       TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                       new Font("Sans Serif",Font.BOLD,20)));
        this.add(timePanel,"North");
        this.add(scrollPane,"Center");

        this.addKeyListener(k);
        viewPanel.addKeyListener(k);
        addAllButton.addKeyListener(k);
        list.addKeyListener(k);

        parsedateformat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss",Locale.ENGLISH);
        printtimeformat = new SimpleDateFormat("hh:mm a",Locale.ENGLISH);
        printdatetimeformat = new SimpleDateFormat("dd/MM/yyyy hh:mm a",Locale.ENGLISH);
        printdateformat = new SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH);

        loadAnnouncements();

        //Check for new announcements every 3 seconds
        reload_event = new ReloadAnnouncementsEvent(this);
        reload_eventcontroller = new TimedEventController(reload_event,Calendar.SECOND,3);
        reloadThread = new Thread(reload_eventcontroller);
        reloadThread.start();

        //Check that we haven't gone past an announcement every 1 seconds
        announcement_event = new AnnouncementTimeEvent(this);
        announcement_eventcontroller = new TimedEventController(announcement_event,Calendar.SECOND,1);
        announcementThread = new Thread(announcement_eventcontroller);
        announcementThread.start();

        flashThread = null;
    }

    //the announcements panel is waiting if there are mediaItems not in the playlist
    public boolean isWaiting() {
        Object[] mediaItems = list.getComponents();
        for (int i = 0; i < mediaItems.length; i ++) {
            if (!((MediaItem)mediaItems[i]).isInPlaylist()) {
                ErrorHandler.info("There are announcements waiting");
                return(true);
            }
        }
        ErrorHandler.info("No announcements waiting");
        return(false);
    }

    /** Checks and removes any mediaItems in the list that have been removed.
        Returns true if any remain after this operation. **/


    public boolean files_exist() {
        if (list.getComponentCount() <= 0) return(false);
        for (int i=0; i < list.getComponentCount(); i++) {
            MediaItem m = (MediaItem) list.getComponent(i);
            String filename = parsedateformat.format(nextAnnouncementTime.getTime())+";"+m.getFilename();
//            try {
                File file = new File(ScheduleDir+"/"+filename);
                if (!file.exists()) {
                    ErrorHandler.info("File disappeared!");
                    playlist.removeMediaItem(m);
                    list.remove(m);
                    i = 0;  //start again because we don't want to miss any
                }
//            }
//            catch (IOException e) {
//                ErrorHandler.error("Something strange happened while trying to remove missing files from the announcementpanel.: "+e+"\nFile concerned:"+filename);
//            }
        }
        if (list.getComponentCount() <= 0) return(false);
        else return(true);
    }

    public void loadAnnouncements() {
        if (!files_exist()) {
            File dir = new File(".",ScheduleDir);
            if (!dir.exists()) dir.mkdirs();
            if (!dir.isDirectory()) {
                 JOptionPane.showMessageDialog(null, "You have a Schedule file, but I was looking\nfor a Schedule Directory.\nPlease accept my apologies.  I don't know what I should do here.  If you can think of what I should do, please contact the Flywire team.", "Alert", JOptionPane.ERROR_MESSAGE); 
                 System.exit(-1);
            }

            File mediaDir = new File(".",ScheduleMediaDir);
            if (!mediaDir.exists()) mediaDir.mkdirs();
            if (!mediaDir.isDirectory()) {
                 JOptionPane.showMessageDialog(null, "You have a ScheduledMedia file, but I was looking\nfor a Scheduled Media Directory.\nPlease accept my apologies.  I don't know what I should do here.  If you can think of what I should do, please contact the Flywire team.", "Alert", JOptionPane.ERROR_MESSAGE); 
                 System.exit(-1);
            }

            File logDir = new File(".",LogDir);
            if (!logDir.exists()) logDir.mkdirs();
            if (!logDir.isDirectory()) {
                 JOptionPane.showMessageDialog(null, "You have a Log file, but I was looking\nfor a Log Directory.\nPlease accept my apologies.  I don't know what I should do here.  If you can think of what I should do, please contact the Flywire team.", "Alert", JOptionPane.ERROR_MESSAGE); 
                 System.exit(-1);
            }

            File[] media = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    boolean isDir = (new File(dir,name)).isDirectory();
                    boolean isHidden = (new File(dir,name)).isHidden();
                    return((!isDir) && (!isHidden));
                } 
            });

            //ErrorHandler.info("Loading Schedule from Dir:"+dir);

            //Calendar currentTime = (Calendar) nextAnnouncementTime.clone();
            //if (currentTime.getTimeInMillis() == Long.MAX_VALUE) {
            //    currentTime = Calendar.getInstance();
            //}

            Calendar proposedMediaTime = (Calendar) nextAnnouncementTime.clone();
            proposedMediaTime.setTimeInMillis(Long.MAX_VALUE);
            //ErrorHandler.info("Current:"+currentTime.getTime());
            //ErrorHandler.info("Proposed:"+proposedMediaTime.getTime());

            //find the next time
            for (int i = 0; i < media.length; i++) {
                String m = media[i].getName();

                StringTokenizer st = new StringTokenizer(m,";");
                Date d = new Date();
                try {
                    d = parsedateformat.parse(st.nextToken().trim());
                } catch (ParseException e) { ErrorHandler.error(e); }
                String mediaFilename = st.nextToken().trim();
               
                File mediaFile = new File(ScheduleMediaDir,mediaFilename);
                if (mediaFile.exists()) {                
                    Calendar mediaTime = Calendar.getInstance();
                    mediaTime.setTime(d);
                //  ErrorHandler.info("Current:"+currentTime.getTime());
                    ErrorHandler.info("Read:"+mediaTime.getTime());
                    if (mediaTime.before(proposedMediaTime)) {
                            ErrorHandler.info("Read and Setting:"+mediaTime.getTime());
                            proposedMediaTime = mediaTime;
                    }
                    ErrorHandler.info("Possible:"+mediaTime.getTime());
                    ErrorHandler.info("Proposed:"+proposedMediaTime.getTime());
                } else {
                    ErrorHandler.info("Could not find media for item:"+m+". Ignoring...");
                }

                try {
                    Thread.sleep(5);
                }
                catch (InterruptedException ie) {
                }
            }

            //if (nextAnnouncementTime.getTimeInMillis() == Long.MAX_VALUE)
            //    nextAnnouncementTime.setTimeInMillis(0);

            nextAnnouncementTime = proposedMediaTime;

            if (proposedMediaTime.getTimeInMillis() == Long.MAX_VALUE) {
                flashThread = null;
                timeLabel.setForeground(Color.black);
                timeLabel.setText("No announcements");
                return;
            }

            //nextAnnouncementTime.setTimeZone(TimeZone.getTimeZone("GMT+10"));


            if (printdateformat.format(nextAnnouncementTime.getTime()).equals(printdateformat.format(Calendar.getInstance().getTime()))) {
                timeLabel.setText(printtimeformat.format(nextAnnouncementTime.getTime()));
            } else {
                timeLabel.setText(printdatetimeformat.format(nextAnnouncementTime.getTime()));
            }

            //load media with that time
            for (int i = 0; i < media.length; i++) {
                String m = (String) media[i].getName();
                StringTokenizer st = new StringTokenizer(m,";");
                Date d = new Date();
                try {
                    d = parsedateformat.parse(st.nextToken().trim());
                } catch (ParseException e) { ErrorHandler.error(e); }
                Calendar mediaTime = Calendar.getInstance();
                mediaTime.setTime(d);
                if (mediaTime.equals(proposedMediaTime)) {
                    loadMedia(st.nextToken().trim());
                }
                ErrorHandler.info("Loading "+m);
            }
            list.revalidate();
        }
    }

    /** Removes the specified mediaitem from the playlist **/


//    public void remove(Announcement mediaitem) {
//            //if (list.indexOf(mediaitem) >= currentItemNumber) currentItemNumber = 0;
//            //mediaitem.clearNumber();
//            list.remove(mediaitem);
//            //Object[] mediaitems = list.getComponents();
//            //for (int i = 0; i < mediaitems.length; i++) {
//            //        MediaItem m = (MediaItem)mediaitems[i];
//
//            //        m.setNumber(i+1);
//            //}
//            checkAnnouncementTime();
//            if (list.getComponentCount() <= 0) loadAnnouncements();
//    }


    void loadMedia(String filename) {
        File dir = new File(".",ScheduleMediaDir);

        if (!dir.exists()) dir.mkdirs();
        if (!dir.isDirectory()) {
            JOptionPane.showMessageDialog(null, "You have a ScheduleMedia file, but I was looking\nfor a ScheduleMedia Directory.\nI cannot continue.\nPlease accept my apologies.\nI don't know what I should do here.\nIf you can think of what I should do,\nplease contact the Flywire team.", "Alert", JOptionPane.ERROR_MESSAGE); 
            System.exit(-1);
        }

        File file = new File(ScheduleMediaDir,filename);
        ErrorHandler.info("Loading media:"+filename);
        Announcement m = new Announcement(playcontrol, playlist, this, file, "", this);
        if (m == null) ErrorHandler.error("MediaItem is null!");
        //playcontrol.addMedia(filename);
        list.add(m);
    }

    public Calendar getNextTime() {
        return(nextAnnouncementTime);
    }

    public void checkAnnouncementTime() {
        Calendar currentTime = Calendar.getInstance();
        //ErrorHandler.info("currentTime:"+currentTime.getTime());
        //ErrorHandler.info("NextTime:"+nextAnnouncementTime.getTime());

        if (currentTime.after(nextAnnouncementTime)) {
            if (flashThread == null) {
                flashThread = new Thread(this);
                flashThread.start();
            }
        } else {
            flashThread = null;
            //timePanel.setBackground(Color.lightGray);
            timeLabel.setForeground(Color.black);
        }
    }

    public MediaItem nextMediaItem() {
        Object[] mediaItems = list.getComponents();
        for (int i = 0; i < mediaItems.length; i ++) {
            if (!((MediaItem)mediaItems[i]).isInPlaylist()) {
                return((MediaItem)mediaItems[i]);
            }
        }
        return(null);
    }


    public void mouseClicked(MouseEvent e) {
        Object[] mediaItems = list.getComponents();
        for (int i = 0; i < mediaItems.length; i ++) {
            MediaItem m = (MediaItem) mediaItems[i];
            m.addToPlaylist();
        }
    }
    public void mouseEntered(MouseEvent e) { ; }
    public void mouseExited(MouseEvent e) { ; }
    public void mousePressed(MouseEvent e) { ; }
    public void mouseReleased(MouseEvent e) { ; }


    public void run() {
        while (flashThread != null) {
            if (timeLabel.getForeground() == Color.black) {
                timeLabel.setForeground(Color.red.brighter().brighter());
            } else {
                timeLabel.setForeground(Color.black);
            }

            try {
                Thread.sleep(750);
            }
            catch (InterruptedException ie) {
            }
        }
        timeLabel.setForeground(Color.black);
    }

    public void played(MediaItem m) {
        String logfilename = parsedateformat.format(nextAnnouncementTime.getTime())+";"+m.getFilename();
        try {
            FileOutputStream logfile = new FileOutputStream(LogDir+"/"+logfilename+".played");
            PrintWriter pw = new PrintWriter(logfile,true);
            Calendar c = Calendar.getInstance();
            pw.println(parsedateformat.format(c.getTime()));
            logfile.close();
        }
        catch (FileNotFoundException fnfe) {
            ErrorHandler.error("Could not save log. Error: "+fnfe+"\nFile concerned:"+logfilename);
        }
        catch (IOException e) {
            ErrorHandler.error("Could not save log. Error: "+e+"\nFile concerned:"+logfilename);
        }
        File schedulefile = new File(ScheduleDir+"/"+logfilename);
        schedulefile.delete();
        remove(m);
    } //For selectionList
    public void remove(MediaItem m) {
        list.remove(m);
        checkAnnouncementTime();
        loadAnnouncements();
    }


}

class Announcement extends MediaItem {
    public static final long serialVersionUID = 1L; //Why do we do this?

    AnnouncementsPanel p;

    public Announcement(PlayControl playcontrol, PlayList playlist, SelectionList selectionList, AnnouncementsPanel p) {
        super(playcontrol,playlist,selectionList);
        this.p = p;
    }

    public Announcement(PlayControl playcontrol, PlayList playlist, SelectionList selectionList, File file, String description, AnnouncementsPanel p) {
        super(playcontrol,playlist,selectionList,file,description);
        this.p = p;
    }

    public void played() {
        p.played(this);
        //playlist.removeMediaItem(this);
    }
}

class ReloadAnnouncementsEvent extends TimedEvent {
    AnnouncementsPanel p;
    public ReloadAnnouncementsEvent(AnnouncementsPanel p) {
        this.p = p;
    }

    public void doEvent() {
        //ErrorHandler.info("Loading new announcements via event");
        p.loadAnnouncements();
    }
}

class AnnouncementTimeEvent extends TimedEvent {
    AnnouncementsPanel p;
    public AnnouncementTimeEvent(AnnouncementsPanel p) {
        this.p = p;
    }

    public void doEvent() {
        //ErrorHandler.info("Checking announcement time via event");
        p.checkAnnouncementTime();
    }
}
