/*
* Copyright 2004-2009 by dronten@gmail.com
*
* This source is distributed under the terms of the GNU PUBLIC LICENSE version 3
* http://www.gnu.org/licenses/gpl.html
*/


package com.googlepages.dronten.jripper.util;

import java.util.prefs.Preferences;


/**
 * User preference object.
 */
public class Pref {
    private static Pref     PREF = null;
    private Preferences     aPreferences = null;


    /**
     * Create preference object.
     *
     * @param prefName
     */
    public Pref(String prefName) {
        PREF = this;
        aPreferences = Preferences.userRoot().node(prefName);
    }


    /**
     * Get preference object.
     *
     * @return
     */
    public static Preferences get() {
        assert PREF != null;
        return PREF.aPreferences;
    }


    /**
     * Get preference value.
     *
     * @return
     */
    public static String getPref(String key, String def) {
        return PREF.aPreferences.get(key, def);
    }


    /**
     * Get preference value.
     *
     * @return
     */
    public static int getPref(String key, int def) {
        return PREF.aPreferences.getInt(key, def);
    }


    /**
     * Get preference value.
     *
     * @return
     */
    public static double getPref(String key, double def) {
        return PREF.aPreferences.getDouble(key, def);
    }


    /**
     * Get preference value.
     *
     * @return
     */
    public static boolean getPref(String key, boolean def) {
        return PREF.aPreferences.getBoolean(key, def);
    }


    /**
     * Get preference value.
     *
     * @return
     */
    public static int getPref(String key, int option, int def) {
        String key2 = String.format("%s%03d", key, option);
        return PREF.aPreferences.getInt(key2, def);
    }


    /**
     * Get preference value.
     *
     * @return
     */
    public static String getPref(String key, int option, String def) {
        String key2 = String.format("%s%03d", key, option);
        return PREF.aPreferences.get(key2, def);
    }


    /**
     * Set preference.
     *
     * @param val
     */
    public static void setPref(String key, String val) {
        PREF.aPreferences.put(key, val);
    }


    /**
     * Set preference.
     *
     * @param val
     */
    public static void setPref(String key, int val) {
        PREF.aPreferences.putInt(key, val);
    }


    /**
     * Set preference.
     *
     * @param val
     */
    public static void setPref(String key, double val) {
        PREF.aPreferences.putDouble(key, val);
    }


    /**
     * Set preference.
     *
     * @param val
     */
    public static void setPref(String key, boolean val) {
        PREF.aPreferences.putBoolean(key, val);
    }
}
