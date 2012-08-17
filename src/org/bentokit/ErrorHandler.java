package org.bentokit;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.ArrayList;

import org.bentokit.ErrorListener;

// ErrorHandler handles all the errors that occur in the program.
// This way we can block out all errors of a particular type,
// or pipe the errors to a logfile that can be read offsite.

public class ErrorHandler {
    private static boolean DEBUG_ERRORHANDLER = false;

    public static ArrayList<ErrorListener> listeners;
    public static HashMap<String,Boolean> fileoptions;
    public static HashMap<String,Boolean> consoleoptions;

    static String lastConsoleError = null;
    static int lastConsoleErrorCount = 0;

    static String lastFileError = null;
    static int lastFileErrorCount = 0;

    public static boolean PRINT_OFF = false;
    static boolean FILE_OFF = false;

    static FileWriter fileStream;
    static Calendar calendar;
    static String prevFileErrorDate;
    static String prevConsoleErrorDate;
    static java.text.SimpleDateFormat stampformat;
    static java.text.SimpleDateFormat dateformat;
    static java.text.SimpleDateFormat timeformat;

    public static void initialise(String[] args) {
        listeners = new ArrayList<ErrorListener>();
        fileoptions = new HashMap<String,Boolean>();
        consoleoptions = new HashMap<String,Boolean>();        
        //Default False options
        for (String option : new String[] { "MUST","INFO","MEM","WARN","ERROR","UINFO","UERR", "UWARN", "USUCCESS" }) {
            fileoptions.put(option,false);
            consoleoptions.put(option,false);
        }
        //Default True options
        for (String option : new String[] { "FATAL" }) {
            fileoptions.put(option,true);
            consoleoptions.put(option,true);
        }
        
        processSwitches(args);
        
        calendar = GregorianCalendar.getInstance();
        stampformat = new java.text.SimpleDateFormat("yyyy-MM-dd HHmmss");
        dateformat = new java.text.SimpleDateFormat("yyyy-MM-dd");
        timeformat = new java.text.SimpleDateFormat("HH:mm:ss.SSS");
        prevFileErrorDate = "";
        prevConsoleErrorDate = "";

        //Open log file for writing
        if (!FILE_OFF && (fileoptions.get("MUST") || fileoptions.get("INFO") || fileoptions.get("ERROR") || fileoptions.get("FATAL") || fileoptions.get("UINFO") || fileoptions.get("UERR") || fileoptions.get("UWARN") || fileoptions.get("USUCCESS")))
            openLogfile();
        else fileStream = null;
    }

    public static void addListener(ErrorListener listener) { listeners.add(listener); }

    public static void removeListener(ErrorListener listener) { listeners.remove(listener); }
    
    
    private static void processSwitches(String[] args) {
        //Find the --debug arg
        int debugarg = -1;
        int i = 0;
        for (String arg : args) {
            if (arg.startsWith("--debug")) debugarg = i;
            i++;
        }
        if (debugarg < 0) { // We didn't find it
           if (DEBUG_ERRORHANDLER) System.err.println("No debug arg found");
           return;
        }
        if (DEBUG_ERRORHANDLER) System.err.println("Debug arg found at "+debugarg);
        //We did find it.
        //Are we using '=' option
        String optionsstr;
        if (args[debugarg].length() > "--debug".length() && args[debugarg].substring("--debug".length(),"--debug".length()+1).equals("=")) {
            //We're using the = option
            optionsstr = args[debugarg].substring("--debug".length()+1);
        } else {
            //We could be using the next option
            if (args.length > debugarg+1) {
                optionsstr = args[debugarg+1];
            } else {
                optionsstr = "";
            }
        }
        boolean optionset = false;
        if (optionsstr.length() > 0) {
            for (String option : optionsstr.split(",")) {
                boolean optionvalid = false;
                for (String validoption : new String[] { "MUST","INFO","MEM","WARN","ERROR","FATAL" }) {
                    if (option.toUpperCase().equals(validoption)) optionvalid = true;
                }
                if (optionvalid) {
                    fileoptions.put(option.toUpperCase(),true);
                    consoleoptions.put(option.toUpperCase(),true);
                    optionset = true;
                }
            }
        }
        if (!optionset) {
            for (String option : new String[] { "MUST","INFO","MEM","WARN","ERROR","FATAL" }) {
                fileoptions.put(option,true);
                consoleoptions.put(option,true);
            }
        }
        if (DEBUG_ERRORHANDLER) {
            System.err.println("Console Debug Options:");
            for (String option : consoleoptions.keySet()) {
                if (consoleoptions.get(option))
                    System.err.println(option);
            }
            System.err.println("File Debug Options:");
            for (String option : fileoptions.keySet()) {
                if (fileoptions.get(option))
                    System.err.println(option);
            }
        }
    }
    
    static void openLogfile() {
        try {        
            fileStream = new FileWriter("errors-"+stampformat.format(calendar.getTime())+".log",true);
        }
        catch (IOException e) {
            System.err.println("Could not open log file:"+e);
            System.exit(-1);
        }
    }

    static void closeLogfile() {
        try {
            fileStream.close();
        }
        catch (IOException e) {
            System.err.println("Could not close log file:"+e);
            System.exit(-1);
        }
        fileStream = null;
    }

    public static boolean isEnabled(String option) {
        return(consoleoptions.get(option.toUpperCase()) || fileoptions.get(option.toUpperCase()));
    }

    // MustPrints are not necessarily errors, but define
    // a special case where it's more than information, like
    // starting the program.
    public static void must(String s) {
        if (consoleoptions.get("MUST")) printConsole("MUST :"+s);
        if (fileoptions.get("MUST")) printFile("MUST :"+s);
        for (ErrorListener listener : listeners) { listener.must(s); }
    }

    public static void must(Exception e) {
        if (consoleoptions.get("MUST")) printConsole("MUST :"+e.toString());
        if (fileoptions.get("MUST")) printFile("MUST :"+e.toString());
        for (ErrorListener listener : listeners) { listener.must(e.toString()); }
    }

    // Information strings provide possibly superfluous
    // debugging information.
    public static void info(String s) {
        if (consoleoptions.get("INFO")) printConsole("INFO :"+s);
        if (fileoptions.get("INFO")) printFile("INFO :"+s);
        for (ErrorListener listener : listeners) { listener.info(s); }
    }

    public static void info(Exception e) {
        if (consoleoptions.get("INFO")) printConsole("INFO :"+e.toString());
        if (fileoptions.get("INFO")) printFile("INFO :"+e.toString());
        for (ErrorListener listener : listeners) { listener.info(e.toString()); }
    }

    // Information strings provide possibly superfluous
    // debugging information.
    public static void mem(String s) {
        if (consoleoptions.get("MEM")) printConsole("MEM  :"+s);
        if (fileoptions.get("INFO")) printFile("MEM  :"+s);
        for (ErrorListener listener : listeners) { listener.mem(s); }
    }

    public static void mem(Exception e) {
        if (consoleoptions.get("MEM")) printConsole("MEM  :"+e.toString());
        if (fileoptions.get("INFO")) printFile("MEM  :"+e.toString());
        for (ErrorListener listener : listeners) { listener.mem(e.toString()); }
    }

    // Warnings mean something could possibly have gone wrong, but we rectified it.
    public static void warn(String s) {
        if (consoleoptions.get("WARN")) printConsole("WARN :"+s);
        if (fileoptions.get("WARN")) printFile("WARN :"+s);
        for (ErrorListener listener : listeners) { listener.warn(s); }
    }

    public static void warn(Exception e) {
        if (consoleoptions.get("ERROR")) printConsole("ERROR:"+e.toString());
        if (fileoptions.get("ERROR")) printFile("ERROR:"+e.toString());
        for (ErrorListener listener : listeners) { listener.warn(e.toString()); }
    }

    // Errors mean the program went wrong somewhere, but
    // it was not serious enough to close the program.
    public static void error(String s) {
        if (consoleoptions == null) System.err.println("ErrorHandler not initialised error!!!");
        else if (consoleoptions.get("ERROR")) printConsole("ERROR:"+s);
        if (fileoptions == null) System.err.println("ErrorHandler not initialised error!!!");
        else if (fileoptions.get("ERROR")) printFile("ERROR:"+s);
        for (ErrorListener listener : listeners) { listener.error(s); }
    }

    public static void error(Exception e) {
        if (consoleoptions == null) System.err.println("ErrorHandler not initialised error!!!");
        else if (consoleoptions.get("ERROR")) printConsole("ERROR:"+e.toString());
        if (fileoptions == null) System.err.println("ErrorHandler not initialised error!!!");
        else if (fileoptions.get("ERROR")) printFile("ERROR:"+e.toString());
        for (ErrorListener listener : listeners) { listener.error(e.toString()); }
    }

    // User Infos mean the something happened that the user should know.
    public static void userInfo(String s) {
        if (consoleoptions == null) System.err.println("ErrorHandler not initialised error!!!");
        else if (consoleoptions.get("UINFO")) printConsole("UINFO:"+s);
        if (fileoptions == null) System.err.println("ErrorHandler not initialised error!!!");
        else if (fileoptions.get("UINFO")) printFile("UINFO:"+s);
        for (ErrorListener listener : listeners) { listener.userInfo(s); }
    }

    // User Errors mean the something happened that the user should correct.
    public static void userError(String s) {
        if (consoleoptions == null) System.err.println("ErrorHandler not initialised error!!!");
        else if (consoleoptions.get("UERR")) printConsole("UERR:"+s);
        if (fileoptions == null) System.err.println("ErrorHandler not initialised error!!!");
        else if (fileoptions.get("UERR")) printFile("UERR:"+s);
        for (ErrorListener listener : listeners) { listener.userError(s); }
    }

    // User warnings means something happened that the user should be careful about.
    public static void userWarn(String s) {
        if (consoleoptions == null) System.err.println("ErrorHandler not initialised error!!!");
        else if (consoleoptions.get("UWARN")) printConsole("UWARN:"+s);
        if (fileoptions == null) System.err.println("ErrorHandler not initialised error!!!");
        else if (fileoptions.get("UWARN")) printFile("UWARN:"+s);
        for (ErrorListener listener : listeners) { listener.userWarn(s); }
    }
    
 // User plugin warnings means something happened plugin-wise that the user should be careful about.
    public static void userPluginWarn(String s) {
        if (consoleoptions == null) System.err.println("ErrorHandler not initialised error!!!");
        else if (consoleoptions.get("UWARN")) printConsole("UWARN:"+s);
        if (fileoptions == null) System.err.println("ErrorHandler not initialised error!!!");
        else if (fileoptions.get("UWARN")) printFile("UWARN:"+s);
        for (ErrorListener listener : listeners) { listener.userPluginWarn(s); }
    }

    //UserProgressBegin indicates a long task started.
    public static void userProgressBegin(String name, int numberOfWorkUnits) {
        for (ErrorListener listener : listeners) { listener.userProgressBegin(name,numberOfWorkUnits); }
    }

    //UserProgressWorked indicates a long task did some work.
    public static void userProgressWorked(String name, int numberOfThingsDone) {
        for (ErrorListener listener : listeners) { listener.userProgressWorked(name, numberOfThingsDone); }
    }

    //UserProgressWorked indicates a long task did some work.
    public static void userProgressWorked(int numberOfThingsDone) {
        for (ErrorListener listener : listeners) { listener.userProgressWorked(numberOfThingsDone); }
    }

    //UserProgressEnd indicates a long task finished.
    public static void userProgressEnd() {
        for (ErrorListener listener : listeners) { listener.userProgressEnd(); }
    }

    //UserProgressFail indicates a long task failed.
    public static void userProgressFail() {
        for (ErrorListener listener : listeners) { listener.userProgressFail(); }
    }


    // User success means something happened that the user should be proud of.
    public static void userSuccess(String s) {
        if (consoleoptions == null) System.err.println("ErrorHandler not initialised error!!!");
        else if (consoleoptions.get("USUCCESS")) printConsole("USUCCESS:"+s);
        if (fileoptions == null) System.err.println("ErrorHandler not initialised error!!!");
        else if (fileoptions.get("USUCCESS")) printFile("USUCCESS:"+s);
        for (ErrorListener listener : listeners) { listener.userSuccess(s); }
    }



    // Fatal errors means that we can't go on like this.
    // Make sure we report the error, then exit.
    public static void fatal(String s, int failcode) {
        if (consoleoptions.get("FATAL")) printConsole("FATAL:"+s);
        if (fileoptions.get("FATAL")) printFile("FATAL:"+s);
        for (ErrorListener listener : listeners) { listener.fatal(s); }
        System.exit(failcode);
    }

    public static void fatal(Exception e, int failcode) {
        if (consoleoptions.get("FATAL")) printConsole("FATAL:"+e.toString());
        if (fileoptions.get("FATAL")) printFile("FATAL:"+e.toString());
        for (ErrorListener listener : listeners) { listener.fatal(e.toString()); }
        System.exit(failcode);
    }

    //Private method for sending information
    static void printConsole(String s) {
        if (!PRINT_OFF) {
            if (lastConsoleErrorCount > 0 && lastConsoleError != null && lastConsoleError.equals(s)) lastConsoleErrorCount++;
            else {
                Calendar calendar = GregorianCalendar.getInstance();
                if (!prevConsoleErrorDate.equals(dateformat.format(calendar.getTime()))) {
                    prevConsoleErrorDate = dateformat.format(calendar.getTime());
                    System.err.println("["+dateformat.format(calendar.getTime())+"]");
                }
                if (lastConsoleErrorCount > 0) {
                    if (lastFileErrorCount > 1) System.err.println(timeformat.format(calendar.getTime())+":Last message repeated "+Integer.toString(lastConsoleErrorCount)+" times.");
                    lastConsoleError = s;
                    lastConsoleErrorCount = 0;
                }
                System.err.println(timeformat.format(calendar.getTime())+":"+s);
                lastConsoleErrorCount++;
            }
        }
    }

    static void printFile(String s) {
        if (fileStream == null && !FILE_OFF && (fileoptions.get("MUST") || fileoptions.get("INFO") || fileoptions.get("MEM") || fileoptions.get("WARN") || fileoptions.get("ERROR") || fileoptions.get("FATAL")))
            openLogfile();
        if (fileStream != null && (FILE_OFF || (!fileoptions.get("MUST") && !fileoptions.get("INFO") && !fileoptions.get("MEM") && !fileoptions.get("WARN") && !fileoptions.get("ERROR") && !fileoptions.get("FATAL"))))
            closeLogfile();
        if (!FILE_OFF) {
            if (lastFileErrorCount > 0 && lastFileError != null && lastFileError.equals(s)) lastFileErrorCount++;
            else {
                calendar = GregorianCalendar.getInstance();
                try {
                    if (!prevFileErrorDate.equals(dateformat.format(calendar.getTime()))) {
                        prevFileErrorDate = dateformat.format(calendar.getTime());
                        fileStream.write("["+dateformat.format(calendar.getTime())+"]\r\n");
                    }
                    if (lastFileErrorCount > 0) {
                        if (lastFileErrorCount > 1) fileStream.write(timeformat.format(calendar.getTime())+":Last message repeated "+Integer.toString(lastFileErrorCount)+" times.\r\n");
                        lastFileError = s;
                        lastFileErrorCount = 0;
                    }
                    fileStream.write(timeformat.format(calendar.getTime())+":"+s+"\r\n");
                    fileStream.flush();
                    lastFileErrorCount++;
                }
                catch (IOException e) {
                    System.err.println("Could not write to log file:"+e);
                    System.exit(-1);
                }
            }
        }
    }

}
