package kr.ac.snu.imageshare;

import android.content.Context;
import android.content.SharedPreferences;

public class ConfigUtil {

    public static final String KEY_SESSION_PREFERENCE = "keys2";
    public static final String keys2Remote1 = "keys2Remote1";
    public static final String keys2Remote2 = "keys2Remote2";
    
    public static String getRemote1(Context context) {
        return getStringValue(context, keys2Remote1);
    }
    
    public static String getRemote2(Context context) {
        return getStringValue(context, keys2Remote2);
    }
    
    public static void setRemote1(Context context, String remote1) {
        saveStringValue(context, keys2Remote1, remote1);
    }
    
    public static void setRemote2(Context context, String remote2) {
        saveStringValue(context, keys2Remote2, remote2);
    }
        
    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(KEY_SESSION_PREFERENCE, Context.MODE_PRIVATE);
    }

    public static int getIntegerValue(Context context, String name) {
        return getPrefs(context).getInt(name, 0);
    }

    public static void saveIntegerValue(Context context, String name, int value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putInt(name, value);
        editor.commit();
    }

    public static String getStringValue(Context context, String name) {
        return getPrefs(context).getString(name, "");
    }

    public static void saveStringValue(Context context, String name, String value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(name, value);
        editor.commit();
    }
}
