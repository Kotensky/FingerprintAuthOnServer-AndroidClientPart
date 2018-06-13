package com.auth.fingerprintauth;

import android.app.Application;
import android.content.Context;

public class FingerprintAuthApp extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        FingerprintAuthApp.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return FingerprintAuthApp.context;
    }

}
