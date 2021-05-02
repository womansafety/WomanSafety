package com.ws.womansafety.utils;

import android.content.Context;

import com.ws.womansafety.App;

import java.util.Set;

public class SharedPrefUtils {
    private static final String PREF_APP = "pref_app";

    static public void saveStringData(String key, String val) {
        App.mContext.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit().putString(key, val).apply();
    }
    static public void saveIntData(String key, int val) {
        App.mContext.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit().putInt(key, val).apply();
    }

    static public void saveBooleanData( String key, boolean val) {
        App.mContext.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit().putBoolean(key, val).apply();
    }

    static public boolean getBooleanData(String key) {
        return App.mContext.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).getBoolean(key, false);
    }

    static public int getIntData(String key,int defVal) {
        return App.mContext.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).getInt(key, defVal);
    }

    static public String getStringData(String key) {
        return App.mContext.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).getString(key, "");
    }

}
