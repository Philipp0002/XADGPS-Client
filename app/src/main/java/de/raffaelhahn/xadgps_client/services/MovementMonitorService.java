package de.raffaelhahn.xadgps_client.services;

import android.app.Activity;
import androidx.core.app.ActivityCompat;

import de.raffaelhahn.xadgps_client.model.Device;
import de.raffaelhahn.xadgps_client.model.NotifyDevice;
import de.raffaelhahn.xadgps_client.R;
import de.raffaelhahn.xadgps_client.Constants;
import de.raffaelhahn.xadgps_client.background.BackgroundWorkService;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;

public class MovementMonitorService {


    public static boolean startMonitoring(Activity context, Device device) {
        if(!isMonitoring(context, device)) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                return false;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = context.getString(R.string.monitor_notify_channel_name);
                String description = context.getString(R.string.monitor_notify_channel_description);
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel("1", name, importance);
                channel.setDescription(description);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this.
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }

            SharedPreferences preferences = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            String notifyDevicesRaw = preferences.getString(Constants.NOTIFY_DEVICES_PREF_KEY, "[]");
            try {
                JSONArray array = new JSONArray(notifyDevicesRaw);
                array.put(device.getAsJson());
                editor.putString(Constants.NOTIFY_DEVICES_PREF_KEY, array.toString());
                editor.apply();
                BackgroundWorkService.createWorkRequestMovementDetection(context);
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean isMonitoring(Context context, NotifyDevice device) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);

        String notifyDevicesRaw = preferences.getString(Constants.NOTIFY_DEVICES_PREF_KEY, "[]");
        try {
            JSONArray array = new JSONArray(notifyDevicesRaw);
            for (int i = 0; i < array.length(); i++) {
                NotifyDevice existingDevice = new NotifyDevice().setFromJson(array.getJSONObject(i));
                if(existingDevice.id.equals(device.id)){
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void stopMonitoring(Context context, NotifyDevice device) {
        if(isMonitoring(context, device)) {
            SharedPreferences preferences = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);

            JSONArray newArray = new JSONArray();
            String notifyDevicesRaw = preferences.getString(Constants.NOTIFY_DEVICES_PREF_KEY, "[]");
            try {
                JSONArray array = new JSONArray(notifyDevicesRaw);
                for (int i = 0; i < array.length(); i++) {
                    NotifyDevice existingDevice = new NotifyDevice().setFromJson(array.getJSONObject(i));
                    if (!existingDevice.id.equals(device.id)) {
                        newArray.put(existingDevice.getAsJson());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.NOTIFY_DEVICES_PREF_KEY, newArray.toString());
            editor.apply();
        }
    }



}
