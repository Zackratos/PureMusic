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
    public static final int SINGER_CYCLE = 2;

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

    public static boolean isRandomPlay(Context context) {
        return getPreferences(context).getBoolean("randomPlay", false);
    }

    public static void putRandomPlay(Context context, boolean randomPlay) {
        getEditor(context).putBoolean("randomPlay", randomPlay);
    }

    public static int getCyclePlay(Context context) {
        return getPreferences(context).getInt("cyclePlay", NO_CYCLE);
    }

    public static void putCyclePlay(Context context, int cyclePlay) {
        getEditor(context).putInt("cyclePlay", cyclePlay);
    }
}
