package com.ws.womansafety;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;

public class App extends Application {

    public static final String PRIMARY_CHANNEL = "default";
    public static Context mContext;


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this.getApplicationContext();
    }

}
