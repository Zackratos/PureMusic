package org.zack.music;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Administrator on 2017/1/12.
 */
public class PreferenceUtil {

    public static final int NO_CYCLE = 0;
    public static final int ALL_CYCLE = 1;
    public static final int SINGLE_CYCLE = 2;

    private static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        return getPreferences(context).edit();
    }

    public static int getCurrent(Context context) {
        return getPreferences(context).getInt("current", 0);
    }

    public static void putCurrent(Context context, int current) {
        getEditor(context).putInt("current", current).apply();
    }


    public static boolean isRandom(Context context) {
        return getPreferences(context).getBoolean("random", false);
    }

    public static void putRandom(Context context, boolean random) {
        getEditor(context).putBoolean("random", random).apply();
    }

    public static int getCycle(Context context) {
        return getPreferences(context).getInt("cycle", NO_CYCLE);
    }

    public static void putCycle(Context context, int cycle) {
        getEditor(context).putInt("cycle", cycle).apply();
    }

    public static boolean isTranBackground(Context context) {
        return getPreferences(context).getBoolean("tranBackground", false);
    }

    public static void setTranBackground(Context context, boolean tranBackground) {
        getEditor(context).putBoolean("tranBackground", tranBackground).apply();
    }
}
