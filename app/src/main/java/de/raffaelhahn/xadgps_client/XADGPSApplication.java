package de.raffaelhahn.xadgps_client;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

public class XADGPSApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
