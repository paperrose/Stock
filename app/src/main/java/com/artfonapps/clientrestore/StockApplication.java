package com.artfonapps.clientrestore;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Emil on 31.08.2016.
 */
public class StockApplication extends com.activeandroid.app.Application{
    private static final String GCM_PREFS = "GCM_prefs";
    private static Context appContext;
    private static SharedPreferences prefs;

    public final static int APPLICATION_CALL_PERMISSION_REQUEST = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        prefs = getSharedPreferences(GCM_PREFS, 0);
    }

    public static Context getContext() {
        return appContext;
    }

    public static SharedPreferences getPrefs() {
        return prefs;
    }


}
