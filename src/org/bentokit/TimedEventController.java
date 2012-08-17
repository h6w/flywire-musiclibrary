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

import java.util.*;

public class TimedEventController implements Runnable {
        TimedEvent e;
        int dividefield, divisor;
        int offsetfield, offset;

        public TimedEventController(TimedEvent e, int dividefield, int divisor) {
            this.e = e;
            this.dividefield = dividefield;
            this.divisor = divisor;
            this.offsetfield = -1;
            this.offset = 0;
        }

        public TimedEventController(TimedEvent e, int dividefield, int divisor, int offsetfield, int offset) {
            this.e = e;
            this.dividefield = dividefield;
            this.divisor = divisor;
            this.offsetfield = offsetfield;
            this.offset = offset;
        }


        public void run() {
            Calendar now;
            Calendar next = Calendar.getInstance(TimeZone.getTimeZone("GMT+10"),Locale.ENGLISH);

            while (true) {
                now = Calendar.getInstance(TimeZone.getTimeZone("GMT+10"),Locale.ENGLISH);
                //ErrorHandler.info("Now: "+now);
                //ErrorHandler.info("Next:"+next);
                if (now.after(next)) {
                    e.doEvent();
                    next = (Calendar)now.clone();
                    int distancetogo = now.get(dividefield) % divisor;
                    next.add(dividefield,divisor-distancetogo);
                    if (offsetfield != -1) {
                        next.set(offsetfield,offset);
                    }
                    try {
                        //ErrorHandler.info("Sleeping for "+(next.getTimeInMillis()-now.getTimeInMillis())+" milliseconds");
                        Thread.sleep(next.getTimeInMillis()-now.getTimeInMillis());
                    }
                    catch (InterruptedException ie) {
                    }

                } else {
                    try {
                        //ErrorHandler.info("Sleeping for 50 milliseconds");
                        Thread.sleep(50);
                    }
                    catch (InterruptedException ie) {
                    }
                }
            }
        }

}
