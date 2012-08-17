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

import java.io.*;
import java.util.*;

public class ListLoader {
        String filename;
        File inputFile;
        BufferedReader in;

        public ListLoader(String filename) {
                this.filename = filename;
        }

        public void open() {
                try {
                        inputFile = new File(filename);
                        in = new BufferedReader(new FileReader(inputFile));
                }
                catch (IOException e) {
                        readError();
                }
        }

//        public String getLine() {
//                int c;
//                StringBuffer line = new StringBuffer();
//                try {
//                        c = in.read();
//                        if (c == -1) return(null);
//                        //Clear the newline characters
//                        while (c == '\n' || c == '\r') c = in.read();
//                        if (c == -1) return(null);
//                        while (c != '\n'  && c != '\r' && c != -1) {
//                                line.append((char)c);
//                                c = in.read();
//                        }
//                }
//                catch (IOException e) {
//                        readError();
//                }
//                return(new String(line));
//        }

        public Vector<String> getAll() {
                Vector<String> lines;
                lines = new Vector<String>();
                String line;
                try {
                    while ((line = in.readLine()) != null)
                            lines.addElement(line);
                } catch (IOException e) { ; }
                return(lines);
        }

        public void close() {
                try {
                        in.close();
                }
                catch (IOException e) {
                        readError();
                }
                in = null;
                inputFile = null;
        }

        public void readError() {
                ErrorHandler.error("ListLoader: I couldn't read from the file \""+filename+"\"");
        }
}
