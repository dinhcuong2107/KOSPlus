package com.example.kosplus.datalocal;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.onesignal.Continue;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;

public class MyApplication extends Application {
    private static MyApplication instance;
    private static final String ONESIGNAL_APP_ID = "7859382a-ab69-4f54-817f-89e7e77e88ed";
    @Override
    public void onCreate() {
        super.onCreate();
        // Bắt buộc chế độ sáng trên toàn bộ app
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        instance = this;
        DataLocalManager.init(getApplicationContext());

        // Enable verbose logging for debugging (remove in production)
        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);
        // Initialize with your OneSignal App ID
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);
        // Use this method to prompt for push notifications.
        // We recommend removing this method after testing and instead use In-App Messages to prompt for notification permission.
        OneSignal.getNotifications().requestPermission(false, Continue.none());
    }

    public static MyApplication getInstance(){
        return instance;
    }
}
