package com.labs.plagueis.testproject.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static com.labs.plagueis.testproject.util.LogUtils.makeLogTag;



/**
 * Created by plagueis on 1/03/15.
 */
public class PrefUtils  {
    private static final String TAG = makeLogTag("PrefUtils");

    public static final String PREF_USER_SIGNED_IN = "pref_user_signed_in";

    public static final String PREF_USER_SIGNED_DONE = "pref_user_signed_done";

    public static void init(final Context context) {
        // Check what year we're configured for
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    }


    public static boolean isUserSignedIn(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_USER_SIGNED_IN, false);
    }

    public static void markUserSignedIn(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_USER_SIGNED_IN, true).commit();
    }

    public static boolean isUserSignedDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_USER_SIGNED_DONE, false);
    }

    public static void markUserSignedDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_USER_SIGNED_DONE, true).commit();
    }


    public static void registerOnSharedPreferenceChangeListener(final Context context,
                                                                SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterOnSharedPreferenceChangeListener(final Context context,
                                                                  SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
