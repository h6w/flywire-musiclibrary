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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.xpath.*;


public class MusicLibraryPanel extends JPanel implements SelectionList, ActionListener {
    public static final long serialVersionUID = 1L; //Why do we do this?

    JTextField searchbox;
    JButton searchbutton;
    PlayControl playcontrol;
    PlayList playlist;
    JPanel viewPanel;
    JPanel searchPanel;
    JPanel list;

    public MusicLibraryPanel(PlayList playlist, PlayControl playcontrol, KeyListener k) {
            this.playcontrol = playcontrol;
            this.playlist = playlist;

            list = new JPanel();
            list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));

            searchbox = new JTextField();
            searchbox.setFont(new Font("Sans Serif",Font.PLAIN,15));

            searchbutton = new JButton();
            searchbutton.setFont(new Font("Sans Serif",Font.PLAIN,15));
            searchbutton.add(new JLabel("Search"));
            searchbutton.addActionListener(this);

            searchPanel = new JPanel();
            searchPanel.setLayout(new BorderLayout());
            searchPanel.add(searchbox,"Center");
            searchPanel.add(searchbutton,"East");

            viewPanel = new JPanel();
            viewPanel.setLayout(new BorderLayout());
            viewPanel.add(list,"North");
            viewPanel.add(new JPanel(),"Center");
            JScrollPane scrollPane = new JScrollPane(viewPanel);

            this.setLayout(new BorderLayout());
            this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Music Library",
                       TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                       new Font("Sans Serif",Font.BOLD,20)));
            this.add(searchPanel,"North");
            this.add(scrollPane,"Center");

            this.addKeyListener(k);
            viewPanel.addKeyListener(k);
            list.addKeyListener(k);
            //searchbox.addKeyListener(k);

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

    synchronized void sortMusicLibrary() {
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

    public void actionPerformed(ActionEvent ae) {
        emptyList();
        try {
            Node results = MusicLibraryUtils.search(this.searchbox.getText());        
            String classname = "null";
            if (results != null) classname = results.getClass().getName();
            System.err.println("Received MusicBrainz data:"+classname);
            javax.xml.xpath.XPath xpath = javax.xml.xpath.XPathFactory.newInstance().newXPath();
            javax.xml.xpath.XPathExpression expr = xpath.compile("//metadata/track-list/track");
            NodeList tracks = (NodeList) expr.evaluate(results, XPathConstants.NODESET);

            if (tracks.getLength() > 0) {
                for (int i = 0; i < tracks.getLength(); i++) {
                    MediaItem trackMI = new MediaItem(this.playcontrol, this.playlist, this);
                                        
                    expr = xpath.compile("//metadata/track-list/track["+(i+1)+"]/title/text()");
                    String title = (String) expr.evaluate(results, XPathConstants.STRING);

                    expr = xpath.compile("//metadata/track-list/track["+(i+1)+"]/artist/name/text()");
                    String artist = (String) expr.evaluate(results, XPathConstants.STRING);

                    expr = xpath.compile("//metadata/track-list/track["+(i+1)+"]/release-list/release[1]/title/text()");
                    String release = (String) expr.evaluate(results, XPathConstants.STRING);

                    if (title == null) title = "";
                    if (artist == null) artist = "";
                    if (release == null) release = "";

                    trackMI.setText(title+" - "+artist+" - "+release);

                    expr = xpath.compile("//metadata/track-list/track["+(i+1)+"]/duration/text()");
                    Double duration = (Double) expr.evaluate(results, XPathConstants.NUMBER);

                    trackMI.setDuration(new javax.media.Time(duration/1000)); //MB duration is in milliseconds, MI in seconds
                    list.add(trackMI);
                }
            }
            else {
                System.err.println("The MusicBrainz lookup failed. Either the cd record was not found or server was busy.");
            }
        }
        catch (NumberFormatException e2) {
            System.err.println("Have you set the port number to something strange?");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
