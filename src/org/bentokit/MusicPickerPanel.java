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

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.File;
import java.io.FileFilter;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.TitledBorder;


public class MusicPickerPanel extends JPanel implements FileFilter, SelectionList {
    public static final long serialVersionUID = 1L; //Why do we do this?

    public static String randomMediaDir = "AutoMedia";

    PlayControl playcontrol;
    PlayList playlist;

    public JLabel panelTitle;

    Vector<String> musicHistory;

    public MusicPickerPanel(PlayList playlist, PlayControl playcontrol) {
        this.playcontrol = playcontrol;
        this.playlist = playlist;

        musicHistory = new Vector<String>();

        panelTitle = new JLabel("Automatic DJ Mode");
        panelTitle.setFont(new Font("Sans Serif",Font.BOLD,20));
        panelTitle.setHorizontalAlignment(JLabel.CENTER);

        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Music Picker",
                       TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                       new Font("Sans Serif",Font.BOLD,20)));
        this.add(panelTitle,"Center");
        this.add(new JLabel("Click \"Switch mode to\" (above) \nto return to Show Media Mode"),"South");


        File dir = new File(".",randomMediaDir);
        if (!dir.exists()) dir.mkdirs();
        if (!dir.isDirectory()) {
                JOptionPane.showMessageDialog(null, "You have a AutoMedia file, but I was looking\nfor an AutoMedia Directory.\nPlease accept my apologies.  I don't know what I should do here.  If you can think of what I should do, please contact the Flywire team.", "Alert", JOptionPane.ERROR_MESSAGE); 
                System.exit(-1);
        }
    }

    public synchronized MediaItem nextMediaItem() {
        String nextFilename;
        //ErrorHandler.info("Running next()");

        if (musicHistory.size() <= 0) {
                nextFilename = selectRandomFile(new File(randomMediaDir+File.separator+"MUSIC"));
                if (nextFilename == "") return(null);
        }
        else {
                File dir = new File(".",randomMediaDir+File.separator+"MUSIC");
                if (!dir.exists()) dir.mkdirs();
                if (!dir.isDirectory()) {
                    JOptionPane.showMessageDialog(null, "You have a AutoMedia/MUSIC file, but I was looking\nfor an AutoMedia/MUSIC Directory.\nPlease accept my apologies.  I don't know what I should do here.  If you can think of what I should do, please contact the Flywire team.", "Alert", JOptionPane.ERROR_MESSAGE); 
                    System.exit(-1);
                }
                File[] filenames = (new File(randomMediaDir+File.separator+"MUSIC")).listFiles(this);  //this refers to FileFilter
                while (musicHistory.size() >= (filenames.length/2))
                    musicHistory.remove(musicHistory.firstElement());

                do {
                    nextFilename = selectRandomFile(new File(randomMediaDir+File.separator+"MUSIC"));
                } while (isInMusicHistory(nextFilename));
        }
        //ErrorHandler.info("Somewhere even later in next()");

        musicHistory.add(nextFilename);
        return(new MediaItem(playcontrol,playlist,this,new File(nextFilename),""));
    }

    public boolean isInMusicHistory(String trackname) {
        for (int i = musicHistory.size()-1; i >= 0; i--) {
            String historytrackname = (String) musicHistory.get(i);
            if (historytrackname.compareTo(trackname) == 0) return(true);
        }
        return(false);
    }


    /** Called by playControl when the media has finished.

        Randomly selects a new track to be played.
    **/

    public void EndOfMediaEvent() {
        //ErrorHandler.info("EndOfMedia -> Updating Playlist");
        //playlist.add(nextTrack());
        //ErrorHandler.info("Finished updating playlist");
    }

    public void setSequence(boolean playInSequence) { ; }

    /** Start playing the media in the playlist in order **/

    public void start() {
        //playcontrol.start();
    }

    /** Stop playing the media in the playlist and move to the next item in the playlist **/

    public void stop() {
            //ErrorHandler.info("StoppedEvent");
         //   playcontrol.stop();
    }

    public long getTotalTime() { return(0); }
    public long getTimeSoFar() { return(0); }


    String selectRandomFile(File directory) {
        File[] filenames = directory.listFiles(this);  //this refers to FileFilter

        if (filenames == null) {
            JOptionPane.showMessageDialog(this,"There are no files in the directory "+directory.getPath(),"Error!",JOptionPane.ERROR_MESSAGE);            
            ErrorHandler.error("No files in directory "+directory.getPath());
            return("");
        }
        else if (filenames.length < 2) {
            JOptionPane.showMessageDialog(this,"There are less than 2 files in the directory "+directory.getPath(),"Error!",JOptionPane.ERROR_MESSAGE);            
            ErrorHandler.error("Less than two files in directory "+directory.getPath());
            return("");
        }

        int fileNumber = Math.round((float)((float)Math.random()*((float)filenames.length-1)));

        return(new String(filenames[fileNumber].getPath()));
    }


    public boolean accept(File file) {
        return(!file.isDirectory() && !file.isHidden());
    }


    public void played(MediaItem m) { ; } // For selectionList
    public void remove(MediaItem m) { ; } // For selectionList

}
