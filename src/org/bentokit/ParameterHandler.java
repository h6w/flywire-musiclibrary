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

import java.io.FileReader;
import java.io.StreamTokenizer;

public class ParameterHandler implements Runnable {
    static String parameterFilename = "parameters";

    static FileReader fileStream;
    static StreamTokenizer tokenStream;
    static Thread thread = null;

    public ParameterHandler() {
        thread = new Thread(this);
        thread.start();
    }

    static void loadParameters() {
        int ttype;
        String parameter;
        String value;

        try {
            fileStream = new FileReader(parameterFilename);
            tokenStream = new StreamTokenizer(fileStream);
            tokenStream.eolIsSignificant(false);
            tokenStream.lowerCaseMode(false);
            tokenStream.wordChars('!','~');
        }
        catch (java.io.IOException e) {
            //ErrorHandler.error("IOException in ParameterHandler.loadParameters() #1:"+e.toString());
            return;
        }

        try {
            ttype = tokenStream.nextToken();
            while (ttype != StreamTokenizer.TT_EOF) {
                if (ttype == StreamTokenizer.TT_WORD)
                    parameter = tokenStream.sval;
                else {
                    ErrorHandler.error("Parameter incorrect on line "+tokenStream.lineno()+" Type:"+ttype_string(ttype));
                    return;
                }
                ttype = tokenStream.nextToken();
                if (ttype == StreamTokenizer.TT_WORD)
                    value = tokenStream.sval;
                else {
                    ErrorHandler.error("Value incorrect on line "+tokenStream.lineno()+" Type:"+ttype_string(ttype));
                    return;
                }
                //switcheroo(parameter,value);
                ttype = tokenStream.nextToken();
            }
        }
        catch (java.io.IOException e) {
            ErrorHandler.error("IOException in ParameterHandler.loadParameters() #2:"+e.toString());
        }

        try {
            fileStream.close();
        }
        catch (java.io.IOException e) {
            ErrorHandler.error("Could not close parameter file");
        }
    }

    public static String ttype_string(int ttype) {
        switch(ttype) {
            case StreamTokenizer.TT_EOF: return("TT_EOF");
            case StreamTokenizer.TT_EOL: return("TT_EOL");
            case StreamTokenizer.TT_NUMBER: return("TT_NUMBER");
            case StreamTokenizer.TT_WORD: return("TT_WORD");
            default: return("UNKNOWN");
        }
    }

/*
    public static void switcheroo(String parameter, String value) {
        boolean truth;

        if (value.compareTo("true") == 0) truth = true;
        else if (value.compareTo("false") == 0) truth = false;
        else { ErrorHandler.must("Unknown Value on line "+tokenStream.lineno()+":\""+value+"\""); return; }

        if (parameter.compareTo("PRINT_MUST") == 0)       ErrorHandler.PRINT_MUST  = truth;
        else if (parameter.compareTo("PRINT_INFO") == 0)  ErrorHandler.PRINT_INFO  = truth;
        else if (parameter.compareTo("PRINT_MEM") == 0)   ErrorHandler.PRINT_MEM   = truth;
        else if (parameter.compareTo("PRINT_ERROR") == 0) ErrorHandler.PRINT_ERROR = truth;
        else if (parameter.compareTo("PRINT_FATAL") == 0) ErrorHandler.PRINT_FATAL = truth;
        else if (parameter.compareTo("FILE_MUST") == 0)   ErrorHandler.FILE_MUST   = truth;
        else if (parameter.compareTo("FILE_INFO") == 0)   ErrorHandler.FILE_INFO   = truth;
        else if (parameter.compareTo("FILE_MEM") == 0)    ErrorHandler.FILE_MEM    = truth;
        else if (parameter.compareTo("FILE_ERROR") == 0)  ErrorHandler.FILE_ERROR  = truth;
        else if (parameter.compareTo("FILE_FATAL") == 0)  ErrorHandler.FILE_FATAL  = truth;
        else if (parameter.compareTo("PRINT_OFF") == 0)   ErrorHandler.PRINT_OFF   = truth;
        else if (parameter.compareTo("FILE_OFF") == 0)    ErrorHandler.FILE_OFF    = truth;
        else ErrorHandler.must("Unknown Parameter on line "+tokenStream.lineno()+":\""+parameter+"\"");
    }
*/

    public void run() {
        while(true) {
            loadParameters();
            try {
                //ErrorHandler.info("Sleeping for 50 milliseconds");
                Thread.sleep(500);
            }
            catch (InterruptedException ie) {
            }
        }
    }
}
