package com.example.kosplus.datalocal;

import android.content.Context;

public class DataLocalManager {
    private DataLocalPreferences dataLocalPreferences;
    private static DataLocalManager instance;
    private static final String UID = "UID";
    private static final String ROLE = "ROLE";
    private static final String FIRST_INSTALL = "FIRST_INSTALL";

    public static void init(Context context) {
        instance = new DataLocalManager();
        instance.dataLocalPreferences = new DataLocalPreferences(context);
    }

    public static DataLocalManager getInstance() {
        if (instance == null) {
            instance = new DataLocalManager();
        }
        return instance;
    }

    public static void setUid(String uid) {
        DataLocalManager.getInstance().dataLocalPreferences.putString(UID, uid);
    }

    public static String getUid() {
        return DataLocalManager.getInstance().dataLocalPreferences.getString(UID, "");
    }

    public static void setRole(String role) {
        DataLocalManager.getInstance().dataLocalPreferences.putString(ROLE, role);
    }

    public static String getRole() {
        return DataLocalManager.getInstance().dataLocalPreferences.getString(ROLE, "");
    }

    public static void setFirstInstall(boolean firstInstall) {
        DataLocalManager.getInstance().dataLocalPreferences.putBoolean(FIRST_INSTALL, firstInstall);
    }

    public static boolean isFirstInstall() {
        return DataLocalManager.getInstance().dataLocalPreferences.getBoolean(FIRST_INSTALL, true);
    }
}
