package com.example.mindcheckdatacollectionapp;

import android.app.Application;
import android.content.Context;

public class MindCheckApplication extends Application {
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        MindCheckApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MindCheckApplication.context;
    }
}
