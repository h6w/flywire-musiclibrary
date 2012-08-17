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
import javax.swing.*;
import org.bentokit.SoftwareUpdater;

class AboutBox extends JFrame {
    public static final long serialVersionUID = 1L; //Why do we do this?

    public AboutBox() {
        this.setLayout(new BorderLayout());
        JPanel pane = new JPanel();
        this.setLayout(new BorderLayout());
        pane.add(new JLabel("Version "+SoftwareUpdater.getThisVersion()),BorderLayout.CENTER);
        this.add(pane,BorderLayout.CENTER);
        this.pack();
        this.setVisible(true);
    }
}
