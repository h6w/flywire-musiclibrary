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
import java.io.File;
import java.text.*;
import java.util.*;
import javax.swing.*;

public class InfoPanel extends JPanel implements Runnable {
    public static final long serialVersionUID = 1L; //Why do we do this?

	PlayList playlist;
	JButton clearAllButton;
	JPanel addressPanel, logoPanel, buttonPanel, timePanel;
	JLabel timeLabel;
	Thread timeThread;
	String[] address = {"Bentokit Project",
	                    "PO Box 12013 A'Beckett St",
	                    "Melbourne",
	                    "VIC    8006",
	                    "phone: (03) 9925 4747",
	                    "email:  info@bentokit.org",
	                    "web:    http://bentokit.org" };
	SimpleDateFormat printtimeformat;

	public InfoPanel(PlayList playlist, PlayControl playcontrol, KeyListener k) {
		this.playlist = playlist;

		//Build the timePanel
		timeLabel = new JLabel("00:00:00");
		timeLabel.setFont(new Font("Sans Serif",Font.BOLD,40));

		//timePanel = new JPanel();
		//timePanel.setLayout(new BoxLayout(timePanel,BoxLayout.Y_AXIS));
		//timePanel.add(new JLabel("Clock:"));
		//timePanel.add(timeLabel);

		//Build the Address
		addressPanel = new JPanel();
		addressPanel.setLayout(new BoxLayout(addressPanel,BoxLayout.Y_AXIS));
		for (int i = 0; i < address.length; i++) {
			JLabel addressLine = new JLabel(address[i]);
			addressLine.setFont(new Font("Sans Serif",Font.PLAIN,13));
			addressPanel.add(addressLine);
		}

		//Build the Logo
		ImageIcon logo = new ImageIcon("bin"+File.separator+"logo.png");
		logoPanel = new JPanel(new BorderLayout());
		logoPanel.add(new JLabel(logo),"Center");
		logoPanel.add(timeLabel,"South");

		//Construct the whole panel
		this.setLayout(new GridLayout(1,2));
		//this.add(new JPanel());
		//this.add(timePanel);
		this.add(addressPanel);
		this.add(logoPanel);

		this.addKeyListener(k);

		printtimeformat = new SimpleDateFormat("hh:mm:ss a",Locale.ENGLISH);

        // Create the clock update thread
        timeThread = new Thread(this);
        timeThread.start();

	}


    public void run() {
		while (true) {
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+10"),Locale.ENGLISH);

  		    timeLabel.setText(printtimeformat.format(c.getTime()));


            try {
					Thread.sleep(50);
			}
			catch (InterruptedException ie) {
			}

		}
	}

}
