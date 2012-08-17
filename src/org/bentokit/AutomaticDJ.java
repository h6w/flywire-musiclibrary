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

import java.util.Calendar;
import java.util.Vector;
import javax.media.Controller;

public class AutomaticDJ {
    public static final int MUSIC = 0;
    public static final int ID = 1;
    public static final int ANNOUNCEMENT = 2;

    PlayList playlist;
    boolean DJisON;

    MusicPickerPanel music;
    IDsPanel ids;
    AnnouncementsPanel announcements;

    Vector<TracknameType> history;
    int maxHistory = 5;
    int maxPlaylistSize = 5;

    public AutomaticDJ(PlayList playlist, MusicPickerPanel music,
                       IDsPanel ids, AnnouncementsPanel announcements) {
        this.playlist = playlist;
        this.music = music;
        this.ids = ids;
        this.announcements = announcements;

        history = new Vector<TracknameType>();

        DJisON = false;
    }

    public void EndOfMediaEvent(Controller player) {
        playlist.EndOfMediaEvent(player);
        if (DJisON) {
            ErrorHandler.info("and DJisON");
            while (playlist.countItems() <= maxPlaylistSize) {
                playlist.add(nextTrack());
            }
            playlist.removePlayedItems();
            while (playlist.countItems() <= maxPlaylistSize) {
                playlist.add(nextTrack());
            }
        } else ErrorHandler.info("but DJisOFF");
    }

    public MediaItem nextTrack() {
        MediaItem nextMediaItem;
        //ErrorHandler.info("Running next()");
        int lastTrackType;
        if (history.size() > 0) {
            TracknameType lastTracknameType = (TracknameType) history.lastElement();
            lastTrackType = lastTracknameType.trackType;
        } else {
            lastTrackType = ID;
        }
        int nextTrackType = MUSIC;
        switch (lastTrackType) {
            case MUSIC:
                //check to see if there are any annoucements outstanding
                //ErrorHandler.info("Next time:"+announcements.getNextTime().getTimeInMillis());
                //ErrorHandler.info("Now:      "+Calendar.getInstance().getTimeInMillis());
                //ErrorHandler.info("Playlist: "+playlist.getTotalTime());
                if ((announcements.getNextTime().getTimeInMillis() <= Calendar.getInstance().getTimeInMillis()+playlist.getTotalTime()) && announcements.isWaiting())
                    nextTrackType = ANNOUNCEMENT;
                //if not, play an id
                else nextTrackType = ID;
                break;
            case ID:
                //nextTrackType = MUSIC;
                nextTrackType = MUSIC;
                break;
            case ANNOUNCEMENT:
                nextTrackType = MUSIC;
                break;
            default:
                ErrorHandler.error("Unknown Last Media Type: defaulting to MUSIC");
                nextTrackType = MUSIC;
                break;
        }
        //String nextFilename;
        //ErrorHandler.info("Somewhere in next()");
        //make sure we haven't picked the same file as the last track/promo
        if (nextTrackType == ANNOUNCEMENT) nextMediaItem = announcements.nextMediaItem();
        else if (lastTracktypeName(nextTrackType) == null) {
            switch (nextTrackType) {
                case MUSIC:
                nextMediaItem = music.nextMediaItem();
                break;
                case ID:
                nextMediaItem = ids.nextMediaItem();
                break;
                default:
                nextMediaItem = music.nextMediaItem();
                break;
            }
        }
        else {
            do {
                switch (nextTrackType) {
                    case MUSIC:
                    nextMediaItem = music.nextMediaItem();
                    break;
                    case ID:
                    nextMediaItem = ids.nextMediaItem();
                    break;
                    default:
                    nextMediaItem = music.nextMediaItem();
                    break;
                }
            }
            while (nextMediaItem != null 
                   &&nextMediaItem.getFilename().compareTo(lastTracktypeName(nextTrackType)) == 0);
        }

        if (nextMediaItem == null) {

            this.stop();
            return(null);
        }
        //ErrorHandler.info("Somewhere later in next()");

        ErrorHandler.info("Adding "+nextMediaItem.getFilename());
        playlist.add(nextMediaItem);
        TracknameType nextTracknameType = new TracknameType();
        nextTracknameType.filename = nextMediaItem.getFilename();
        nextTracknameType.trackType = nextTrackType;
        history.addElement(nextTracknameType);
        while (history.size() > maxHistory) history.remove(history.firstElement());
        //ErrorHandler.info("Ending next()");
        return(nextMediaItem);
    }

    /** Get the name of the last track of a particular type
    **/
    public String lastTracktypeName(int trackType) {
        for (int i = history.size()-1; i >= 0; i--) {
            TracknameType tracknameType = (TracknameType) history.get(i);
            if (tracknameType.trackType == trackType) return(tracknameType.filename);
        }
        return(null);
    }

    public boolean start() {
        MediaItem nextTrack;
        do {
            nextTrack = nextTrack();
            if (nextTrack != null) {
                playlist.add(nextTrack);
                DJisON = true;
                ErrorHandler.info("Auto DJ started");
            } else {
                ErrorHandler.info("Automatic DJ cannot continue.");
                DJisON = false;
                return(false);
            }
        }
        while (nextTrack != null && playlist.countItems() <= maxPlaylistSize);
        return(true);
    }

    public void stop() {
        DJisON = false;
        ErrorHandler.info("Auto DJ stopped");
    }

    public static String getTypeString(int type) {
        switch(type) {
            case MUSIC: return("MUSIC");
            case ID: return("ID");
            case ANNOUNCEMENT: return("ANNOUNCEMENT");
            default: return("UNKNOWN");
        }
    }

    class TracknameType {
        public String filename;
        public int trackType;
    }

}
