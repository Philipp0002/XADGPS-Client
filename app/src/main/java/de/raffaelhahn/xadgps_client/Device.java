package de.raffaelhahn.xadgps_client;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.Iterator;

public class Device {
    public String id;
    public String name;
    public String sn;
    public String groupID;
    public String groupName;
    public String car;
    public String status;
    public String model;
    public String iccid;
    public String icon;
    public String iconid;
    public String olat;
    public String olng;
    public String latitude;
    public String longitude;
    public String StopTime;
    public String iconurl;
    public String course;
    public String coursedesc;
    public String acc;
    public String type;
    public String headImage;
    public String Fortification;
    public String style;
    public String lastCommunication;
    public String speed;
    public String isStop;
    public String distance;
    public String GPS;
    public String GSM;

    public void setFromJson(JSONObject jsonObject) {
        Iterator<String> keys = jsonObject.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            try {
                Field f = Device.class.getDeclaredField(key);
                f.setAccessible(true);
                f.set(this, jsonObject.get(key));
            } catch (Throwable e) {
                Log.w("Device", "setFromJson: " + e.getMessage());
            }
        }
    }

    public String getDeviceInfoShort(Context context) {
        String description = "";
        if(isStop != null) {
            if ("1".equalsIgnoreCase(isStop)) {
                description = context.getString(R.string.device_parked);

                if (StopTime != null) {
                    description += " (" + StopTime + ")";
                }

            } else {
                description = context.getString(R.string.device_moving);
                if (speed != null) {
                    description += " (" + speed + " km/h)";
                }
            }
        }
        return description;
    }
}
