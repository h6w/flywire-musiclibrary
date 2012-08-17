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
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.net.URL;
import java.net.URLClassLoader;


public class Flywire extends JFrame implements KeyListener, Runnable {
    public static final long serialVersionUID = 1L; //Why do we do this?
    public static final int SHOW = 0;
    public static final int AUTODJ = 1;

    PlayControl playcontrol;
    PlayList playlist;
    ChoicePanels choicepanels;

    ShowMediaPanel thisshow;
    AnnouncementsPanel announcements;
    IDsPanel synIDs;
    MusicPickerPanel musicpicker;
    MusicLibraryPanel musiclibrary;
    InfoPanel infopanel;

    Thread memoryThread;
    Runtime runtime;
    public int play_mode = SHOW;

    JMenuBar menubar;
    JMenu switchMenu;
    JMenuItem showModeMenuItem;
    JMenuItem autodjModeMenuItem;
    JMenu helpMenu;
    JMenuItem aboutMenuItem;

    AutomaticDJ autodj;


    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_P) {
            playlist.setSequence(true);
            playlist.start();
        }
    }

    public void keyReleased(KeyEvent e) { ; }
    public void keyTyped(KeyEvent e) { ; }

    public void run() {
        long total = 0, max = 0;
        //long free = 0;
        long gctotal = 0, gcmax = 0;
        //long gcfree = 0;
        while(true) {
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT,Locale.ENGLISH);
            df.setCalendar(Calendar.getInstance());
            if (total != runtime.totalMemory()) {
                total = runtime.totalMemory();
                ErrorHandler.mem("Total:"+runtime.totalMemory());
            }
            if (max != runtime.maxMemory()) {
                max = runtime.maxMemory();
                ErrorHandler.mem("Max:"+runtime.maxMemory());
            }
            runtime.gc();
            if (gctotal != runtime.totalMemory()) {
                gctotal = runtime.totalMemory();
                ErrorHandler.mem("GC Total:"+runtime.totalMemory());
            }
            if (gcmax != runtime.maxMemory()) {
                gcmax = runtime.maxMemory();
                ErrorHandler.mem("GC Max:"+runtime.maxMemory());
            }
            try {
                Thread.sleep(60000);
            }
            catch (InterruptedException ie) {
            }

        }
    }

//    public void run() {
//        while(true) {
//            ErrorHandler.info("Total:"+runtime.totalMemory());
//            ErrorHandler.info("Max:  "+runtime.maxMemory());
//            ErrorHandler.info("Free: "+runtime.freeMemory());
//            runtime.gc();
//            ErrorHandler.info("GC Total:"+runtime.totalMemory());
//            ErrorHandler.info("GC Max:  "+runtime.maxMemory());
//            ErrorHandler.info("GC Free: "+runtime.freeMemory());
//            try {
//                ErrorHandler.info("Sleeping for 10000 milliseconds");
//                Thread.sleep(10000);
//            }
//            catch (InterruptedException ie) {
//            }
//
//        }
//    }


    public Flywire() {
        super("Bentokit Flywire - playout client");

        TimeZone.setDefault(TimeZone.getTimeZone("Australia/Melbourne"));
        //ParameterHandler p = new ParameterHandler();
        ErrorHandler.must("Program Started");

        playcontrol = new PlayControl(this);
        playlist = new PlayList(playcontrol, this);
        playcontrol.setPlayList(playlist);

        thisshow = new ShowMediaPanel(playlist,playcontrol, this);
        musicpicker = new MusicPickerPanel(playlist,playcontrol);
        musiclibrary = new MusicLibraryPanel(playlist,playcontrol, this);
        announcements = new AnnouncementsPanel(playlist,playcontrol, this);
        synIDs = new IDsPanel(playlist,playcontrol, this);
        infopanel = new InfoPanel(playlist,playcontrol, this);

        autodj = new AutomaticDJ(playlist,musicpicker,synIDs,announcements);
        playcontrol.setAutomaticDJ(autodj);

        choicepanels = new ChoicePanels(playcontrol, playlist, this,
                                        thisshow,announcements,
                                        synIDs,musicpicker,musiclibrary,
                                        infopanel);

        //Build the Switch Menu
        showModeMenuItem = new JMenuItem("Show mode");
        showModeMenuItem.setEnabled(false);
        showModeMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                autodjModeMenuItem.setEnabled(true);
                choicepanels.switchMode(SHOW);
                ErrorHandler.info("Switching AutoDJ OFF");
                autodj.stop();
                showModeMenuItem.setEnabled(false);
            }
        });

        autodjModeMenuItem = new JMenuItem("Automatic DJ mode");
        autodjModeMenuItem.setEnabled(true);
        autodjModeMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ErrorHandler.info("Switching AutoDJ ON");
                if (autodj.start()) {
                    choicepanels.switchMode(AUTODJ);
                    showModeMenuItem.setEnabled(true);
                    autodjModeMenuItem.setEnabled(false);
                    ErrorHandler.info("Switching AutoDJ ON succeeded");
                } else {
                    choicepanels.switchMode(SHOW);
                    showModeMenuItem.setEnabled(false);
                    autodjModeMenuItem.setEnabled(true);
                    ErrorHandler.info("Switching AutoDJ ON failed");
                    JOptionPane.showMessageDialog(choicepanels,"Automatic DJ could not be started.","Error!",JOptionPane.ERROR_MESSAGE);            

                }
            }
        });

        switchMenu = new JMenu("Switch mode to");
        switchMenu.add(showModeMenuItem);
        switchMenu.add(autodjModeMenuItem);

        menubar = new JMenuBar();
        menubar.add(switchMenu);

        //Build the Switch Menu
        aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AboutBox about = new AboutBox();
                about.pack();
                about.setVisible(true);
            }
        });

        helpMenu = new JMenu("Help");
        helpMenu.add(aboutMenuItem);

        menubar.add(helpMenu);

        setJMenuBar(menubar);


        runtime = Runtime.getRuntime();
        memoryThread = new Thread(this);
        memoryThread.start();

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(choicepanels,"Center");
        this.getContentPane().add(playcontrol,"South");
        this.addKeyListener(this);
        playcontrol.addKeyListener(this);
        choicepanels.addKeyListener(this);
        this.pack();

        //GraphicsDevice device;
        //device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        //device.setFullScreenWindow(this);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);

        this.setVisible(true);
    }


    public static void main(String[] argv) {
        //Get the System Classloader
        ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();

        //Get the URLs
        URL[] urls = ((URLClassLoader)sysClassLoader).getURLs();

        for(int i=0; i< urls.length; i++)
        {
            System.out.println(urls[i].getFile());
        }       

        System.err.println("java.library.path = " +
        System.getProperty("java.library.path"));
        System.err.println("java.class.path = " +
        System.getProperty("java.class.path"));

        de.jarnbjo.jmf.OggParser a = new de.jarnbjo.jmf.OggParser(); //Check to make sure we can access the class.

        ErrorHandler.initialise(argv);
        Pref prefs = new Pref("org.bentokit.flywire");

        for (String arg : argv) {
            System.err.println("Arg:"+arg);
            if (arg.substring(0,4).equals("-ml=")) {
                System.err.println("MusicLibrary URL:"+arg.substring(4));
                Pref.setPref(Constants.MUSICLIBRARY_SERVER_KEY,arg.substring(4));
            }
        }
        try {
            //Windows Look and Feel
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            //Default Look and Feel
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) { }



        new Flywire();
    }
}

class ChoicePanels extends JPanel {
    public static final long serialVersionUID = 1L; //Why do we do this?

    PlayControl playcontrol;
    PlayList playlist;
    ShowMediaPanel thisshow;
    AnnouncementsPanel announcements;
    IDsPanel synIDs;
    MusicPickerPanel musicpicker;
    MusicLibraryPanel musiclibrary;
    InfoPanel infopanel;
    JPanel westPanel, eastPanel;

    public ChoicePanels(PlayControl playcontrol, PlayList playlist, KeyListener k,
                        ShowMediaPanel thisshow, AnnouncementsPanel announcements,
                        IDsPanel synIDs, MusicPickerPanel musicpicker, MusicLibraryPanel musiclibrary,
                        InfoPanel infopanel) {
        this.playcontrol = playcontrol;
        this.playlist = playlist;
        this.thisshow = thisshow;
        this.announcements = announcements;
        this.synIDs = synIDs;
        this.musicpicker = musicpicker;
        this.musiclibrary = musiclibrary;
        this.infopanel = infopanel;


        westPanel = new JPanel();
        westPanel.setLayout(new GridLayout(4,1));
        //westPanel.setLayout(new BoxLayout(westPanel,BoxLayout.Y_AXIS));
        westPanel.add(thisshow);
        westPanel.add(synIDs);
        westPanel.add(announcements);
        westPanel.add(musiclibrary);

        eastPanel = new JPanel();
        eastPanel.setLayout(new BoxLayout(eastPanel,BoxLayout.Y_AXIS));
        eastPanel.add(infopanel);
        eastPanel.add(playlist);

        this.setLayout(new GridLayout(1,2));
        //this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
        this.add(westPanel);
        this.add(eastPanel);
        this.addKeyListener(k);
    }

    public void switchMode(int mode) {
        switch(mode) {
            case Flywire.AUTODJ:
                westPanel.remove(thisshow);
                westPanel.validate();
                westPanel.add(musicpicker,0);
                westPanel.validate();
                break;
            case Flywire.SHOW:
                westPanel.remove(musicpicker);
                westPanel.validate();
                westPanel.add(thisshow,0);
                westPanel.validate();
                break;
            default:
                ErrorHandler.error("Aaaaaaargh!!!");
                System.exit(-1);
        }
    }

}

