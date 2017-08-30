package com.yanxit.currencies.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefsMgr {
    private static SharedPreferences preferences;

    public static void setString(Context context,String locale, String code){
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(locale,code);
        editor.commit();
    }

    public static String getString(Context context,String locale){
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(locale,null);
    }
}
