package org.bentokit;

import org.bentokit.ErrorListener;

/** A class for listening to errors thrown by the error handler */
public class ErrorAdapter implements ErrorListener {
    public void must(String s) { ; }
    public void info(String s) { ; }
    public void mem(String s) { ; }
    public void warn(String s) { ; }
    public void error(String s) { ; }
    public void fatal(String s) { ; }
    public void userInfo(String s) { ; }
    public void userError(String s) { ; }
    public void userWarn(String s) { ; }
    public void userSuccess(String s) { ; }
    public void userPluginWarn(String s) {;}
    public void userProgressBegin(String name, int numberOfWorkUnits) {;}
    public void userProgressWorked(String name, int numberOfThingsDone) {;}
    public void userProgressWorked(int numberOfThingsDone) {;}
    public void userProgressEnd() {;}
    public void userProgressFail() {;}
}
