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

public class Track {
    private javax.media.Player player;
    private String name;

    public Track(javax.media.Player player, String name) {
        this.player = player;
        this.name = name;
    }

    public javax.media.Player getPlayer() { return(player); }
    public String getName() { return(name); }
    public String toString() {
        return(new String("Track: Name=["+name+"]"));
    }
}
